export default {
  async fetch(request, env, ctx) {
    const url = new URL(request.url);

    // ===== CORS với whitelist =====
    const reqOrigin = request.headers.get("Origin") || "";
    const ORIGINS = new Set([
      "https://lylyhecker.github.io",
      "https://lylyhecker.github.io/iTravel-Guide",
      "http://itravelguide.ddns.net"
    ]);
    const ALLOW_ORIGIN = ORIGINS.has(reqOrigin) ? reqOrigin : null;

    const CORS_HEADERS = {
      ...(ALLOW_ORIGIN ? { "Access-Control-Allow-Origin": ALLOW_ORIGIN } : {}),
      "Access-Control-Allow-Methods": "POST, OPTIONS",
      "Access-Control-Allow-Headers": "Content-Type",
      "Access-Control-Max-Age": "86400",
    };

    // Trả preflight cho trình duyệt
    if (request.method === "OPTIONS") {
      return new Response(null, { status: 204, headers: CORS_HEADERS });
    }

    if (request.method !== "POST") {
      return new Response(JSON.stringify({ error: "Method Not Allowed" }), {
        status: 405,
        headers: { "Content-Type": "application/json", ...CORS_HEADERS },
      });
    }

    if (url.pathname !== "/gemini") {
      return new Response(JSON.stringify({ error: "Not Found" }), {
        status: 404,
        headers: { "Content-Type": "application/json", ...CORS_HEADERS },
      });
    }

    // ===== Đọc request từ client =====
    let body;
    try {
      body = await request.json();
    } catch {
      return new Response(JSON.stringify({ error: "Invalid JSON" }), {
        status: 400,
        headers: { "Content-Type": "application/json", ...CORS_HEADERS },
      });
    }

    let contents = body?.contents;
    if (!contents && body?.prompt) {
      contents = [{ role: "user", parts: [{ text: String(body.prompt) }]}];
    }
    const generationConfig = body?.generationConfig || {
      temperature: 0.7, topK: 40, topP: 0.95, maxOutputTokens: 1024,
    };

    if (!Array.isArray(contents) || contents.length === 0) {
      return new Response(JSON.stringify({ error: "Missing contents" }), {
        status: 400,
        headers: { "Content-Type": "application/json", ...CORS_HEADERS },
      });
    }

    // ===== Gọi Gemini =====
    const model = url.searchParams.get("model") || "gemini-1.5-flash";
    const apiKey = env.GEMINI_API_KEY;
    if (!apiKey) {
      return new Response(JSON.stringify({ error: "Missing GEMINI_API_KEY" }), {
        status: 500,
        headers: { "Content-Type": "application/json", ...CORS_HEADERS },
      });
    }

    const endpoint = `https://generativelanguage.googleapis.com/v1beta/models/${encodeURIComponent(model)}:generateContent?key=${apiKey}`;

    let upstreamRes, data;
    try {
      upstreamRes = await fetch(endpoint, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ contents, generationConfig }),
      });

      if (!upstreamRes.ok && upstreamRes.status >= 500) {
        upstreamRes = await fetch(endpoint, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ contents, generationConfig }),
        });
      }

      data = await upstreamRes.json();
    } catch (err) {
      return new Response(JSON.stringify({ error: `Upstream error: ${err?.message || err}` }), {
        status: 502,
        headers: { "Content-Type": "application/json", ...CORS_HEADERS },
      });
    }

    if (!upstreamRes.ok) {
      return new Response(JSON.stringify({
        error: "Gemini API error",
        status: upstreamRes.status,
        details: data?.error || data
      }), {
        status: upstreamRes.status,
        headers: { "Content-Type": "application/json", ...CORS_HEADERS },
      });
    }

    return new Response(JSON.stringify(data), {
      status: 200,
      headers: { "Content-Type": "application/json", ...CORS_HEADERS },
    });
  },
};

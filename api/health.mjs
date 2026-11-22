export default function handler(req, res) {
  res.statusCode = 200;
  res.setHeader("Content-Type", "application/json; charset=utf-8");
  res.setHeader("Cache-Control", "no-store");
  res.end(
    JSON.stringify({
      status: "ok",
      service: "Al-Noor Digital Quran API",
      oauthConfigured: Boolean(process.env.QF_CLIENT_ID && process.env.QF_CLIENT_SECRET),
      timestamp: new Date().toISOString()
    })
  );
}

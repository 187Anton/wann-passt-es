// Takes comparable screenshots of all candidates with identical inputs.
// Usage: node screenshots.mjs <round> [agent=port ...]
// Example: node screenshots.mjs r2 codex=8081 claude=8082 hermes=8083
import { chromium } from 'playwright';
import { mkdir, writeFile } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';

const [, , round = 'r2', ...pairs] = process.argv;
const targets = pairs.length
  ? pairs.map((pair) => pair.split('='))
  : [['codex', '8081'], ['claude', '8082'], ['hermes', '8083']];

const outDir = new URL(`../screenshots/${round}/`, import.meta.url);
await mkdir(outDir, { recursive: true });

const zone = 'Europe/Berlin';
const today = new Intl.DateTimeFormat('en-CA', { timeZone: zone }).format(new Date());
const plusDays = (iso, days) => {
  const date = new Date(`${iso}T12:00:00Z`);
  date.setUTCDate(date.getUTCDate() + days);
  return date.toISOString().slice(0, 10);
};

const base = {
  locationId: '2950159',
  locationName: 'Berlin',
  activity: 'WALKING',
  minDurationMinutes: '60',
};
const loose = { temperaturePreference: 'ANY', windPreference: 'MODERATE', precipitationPreference: 'LIGHT_RAIN' };
const strict = { temperaturePreference: 'WARM', windPreference: 'WEAK', precipitationPreference: 'DRY' };
const query = (values) => new URLSearchParams(values).toString();

const pages = [
  { name: 'planung', path: `/?${query({ ...base, ...loose, startDate: today, endDate: plusDays(today, 1) })}` },
  { name: 'leer', path: `/?${query({ ...base, ...strict, startDate: today, endDate: today })}` },
  { name: 'fehler', path: `/?${query({ ...base, ...loose, startDate: plusDays(today, 1), endDate: today })}` },
  { name: 'ortssuche', path: `/locations?${query({ ...base, ...loose, query: 'Potsdam' })}` },
  { name: 'profile', path: '/profiles' },
  { name: 'bearbeiten', path: null },
  { name: '404', path: '/gibtsnicht' },
];
const widths = [360, 1280];
const results = [];

const browser = await chromium.launch();
for (const [agent, port] of targets) {
  const origin = `http://localhost:${port}`;
  for (const width of widths) {
    const context = await browser.newContext({ viewport: { width, height: 800 }, locale: 'de-DE', timezoneId: zone });
    const page = await context.newPage();

    // Ensure a profile named "Testprofil" exists (a duplicate on reruns is fine).
    await page.request.post(`${origin}/profiles`, {
      form: { ...base, ...loose, profileName: 'Testprofil' },
      maxRedirects: 0,
    }).catch(() => {});

    for (const entry of pages) {
      const file = `${agent}-${entry.name}-${width}.png`;
      try {
        let path = entry.path;
        if (entry.name === 'bearbeiten') {
          await page.goto(`${origin}/profiles`);
          path = await page.locator('a[href*="/edit"]').first().getAttribute('href', { timeout: 5000 });
        }
        const response = await page.goto(`${origin}${path}`, { waitUntil: 'networkidle' });
        const overflow = await page.evaluate(() => document.documentElement.scrollWidth > window.innerWidth);
        await page.screenshot({ path: fileURLToPath(new URL(file, outDir)), fullPage: true });
        if (entry.name === 'planung' && width === 1280) {
          await page.screenshot({ path: fileURLToPath(new URL(`${agent}-planung-1280-sichtbar.png`, outDir)) });
        }
        results.push({ agent, page: entry.name, width, status: response?.status(), horizontalOverflow: overflow, file });
        console.log(`ok   ${file} (HTTP ${response?.status()}${overflow ? ', horizontal overflow!' : ''})`);
      } catch (error) {
        results.push({ agent, page: entry.name, width, error: String(error) });
        console.log(`FAIL ${file}: ${error.message}`);
      }
    }
    await context.close();
  }
}
await browser.close();

await writeFile(new URL('ergebnis.json', outDir), JSON.stringify({ round, date: today, targets, results }, null, 2));

const agents = targets.map(([agent]) => agent);
const rows = pages.flatMap((entry) => widths.map((width) => `
  <h2>${entry.name} · ${width} px</h2>
  <div class="row">${agents.map((agent) => `
    <figure><figcaption>${agent}</figcaption><a href="${agent}-${entry.name}-${width}.png"><img src="${agent}-${entry.name}-${width}.png" alt="${agent} ${entry.name} ${width}"></a></figure>`).join('')}
  </div>`)).join('');
await writeFile(new URL('index.html', outDir), `<!DOCTYPE html>
<html lang="de"><head><meta charset="utf-8"><title>Screenshots ${round}</title>
<style>body{font-family:system-ui,sans-serif;margin:1.5rem}h2{margin-top:2.5rem}
.row{display:grid;grid-template-columns:repeat(${agents.length},1fr);gap:1rem;align-items:start}
figure{margin:0}figcaption{font-weight:600;margin-bottom:.4rem}img{width:100%;border:1px solid #999}</style>
</head><body><h1>Screenshots ${round} (${today})</h1>${rows}</body></html>`);
console.log(`\nFertig: ${fileURLToPath(new URL('index.html', outDir))}`);

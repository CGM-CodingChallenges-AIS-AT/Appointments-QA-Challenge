import { expect, test, type APIRequestContext } from '@playwright/test'

/**
 * Hilfsfunktion: legt einen frischen freien Termin-Slot an.
 * Dadurch sind die Tests wiederholbar und unabhaengig von den Seed-Daten.
 */
async function neuerSlot(request: APIRequestContext, stundenInDerZukunft: number) {
  const beginn = new Date(Date.now() + stundenInDerZukunft * 60 * 60 * 1000)
  const pad = (wert: number) => String(wert).padStart(2, '0')
  const lokal =
    `${beginn.getFullYear()}-${pad(beginn.getMonth() + 1)}-${pad(beginn.getDate())}` +
    `T${pad(beginn.getHours())}:${pad(beginn.getMinutes())}:${pad(beginn.getSeconds())}`

  const antwort = await request.post('/api/slots', {
    data: { arzt: 'Dr. Testlauf', beginn: lokal },
  })
  expect(antwort.status()).toBe(201)
  return await antwort.json() as { id: string; arzt: string; beginn: string }
}

test('bucht einen freien Termin ueber die Oberflaeche', async ({ page, request }) => {
  const slot = await neuerSlot(request, 72)
  const patient = `Testpatient ${Date.now()}`

  await page.goto('/')
  await page.getByLabel('Termin-ID').fill(slot.id)
  await page.getByLabel('Patientenname').fill(patient)
  await page.getByLabel('Grund des Besuchs').fill('Jahreskontrolle')
  await page.getByRole('button', { name: 'Termin buchen' }).click()

  await expect(page.getByRole('status')).toHaveText('Termin gebucht.')
  await expect(page.getByRole('cell', { name: patient })).toBeVisible()
})

test('lehnt die zweite Buchung desselben Termins ab', async ({ request }) => {
  const slot = await neuerSlot(request, 96)

  const erste = await request.post('/api/appointments', {
    data: { slotId: slot.id, patientenname: 'Erste Buchung', grund: 'Kontrolle' },
  })
  expect(erste.status()).toBe(201)

  const zweite = await request.post('/api/appointments', {
    data: { slotId: slot.id, patientenname: 'Zweite Buchung', grund: 'Kontrolle' },
  })
  expect(zweite.status()).toBe(409)
})

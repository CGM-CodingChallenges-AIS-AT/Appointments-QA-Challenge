import { useCallback, useEffect, useState, type FormEvent } from 'react'
import './App.css'

type Slot = {
  id: string
  arzt: string
  beginn: string
  gebucht: boolean
}

type Termin = {
  id: string
  slotId: string
  arzt: string
  beginn: string
  patientenname: string
  grund: string
  erstelltAm: string
}

function formatiere(zeitpunkt: string): string {
  const datum = new Date(zeitpunkt)
  return Number.isNaN(datum.getTime()) ? zeitpunkt : datum.toLocaleString('de-AT')
}

async function fehlertext(antwort: Response): Promise<string> {
  try {
    const koerper = await antwort.json() as { fehler?: string; message?: string }
    return koerper.fehler ?? koerper.message ?? `Anfrage fehlgeschlagen (${antwort.status})`
  } catch {
    return `Anfrage fehlgeschlagen (${antwort.status})`
  }
}

function App() {
  const [slots, setSlots] = useState<Slot[]>([])
  const [termine, setTermine] = useState<Termin[]>([])
  const [slotId, setSlotId] = useState('')
  const [patientenname, setPatientenname] = useState('')
  const [grund, setGrund] = useState('')
  const [meldung, setMeldung] = useState('')
  const [fehler, setFehler] = useState('')

  const laden = useCallback(async () => {
    try {
      const [slotAntwort, terminAntwort] = await Promise.all([
        fetch('/api/slots'),
        fetch('/api/appointments'),
      ])
      if (!slotAntwort.ok) throw new Error(await fehlertext(slotAntwort))
      if (!terminAntwort.ok) throw new Error(await fehlertext(terminAntwort))
      setSlots(await slotAntwort.json() as Slot[])
      setTermine(await terminAntwort.json() as Termin[])
    } catch (ursache) {
      setFehler(ursache instanceof Error ? ursache.message : 'Daten konnten nicht geladen werden.')
    }
  }, [])

  useEffect(() => {
    void laden()
  }, [laden])

  async function buchen(ereignis: FormEvent<HTMLFormElement>) {
    ereignis.preventDefault()
    setFehler('')
    setMeldung('')
    try {
      const antwort = await fetch('/api/appointments', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ slotId, patientenname, grund }),
      })
      if (!antwort.ok) throw new Error(await fehlertext(antwort))
      setSlotId('')
      setPatientenname('')
      setGrund('')
      setMeldung('Termin gebucht.')
      await laden()
    } catch (ursache) {
      setFehler(ursache instanceof Error ? ursache.message : 'Termin konnte nicht gebucht werden.')
    }
  }

  async function stornieren(id: string) {
    setFehler('')
    setMeldung('')
    try {
      const antwort = await fetch(`/api/appointments/${id}`, { method: 'DELETE' })
      if (!antwort.ok) throw new Error(await fehlertext(antwort))
      setMeldung('Termin storniert.')
      await laden()
    } catch (ursache) {
      setFehler(ursache instanceof Error ? ursache.message : 'Termin konnte nicht storniert werden.')
    }
  }

  const freieSlots = slots.filter((slot) => !slot.gebucht)

  return (
    <main>
      <h1>Terminbuchung</h1>
      <p>Freie Termine einer Ordination buchen und wieder stornieren.</p>

      <section>
        <h2>Freie Termine</h2>
        {freieSlots.length === 0 ? (
          <p>Derzeit sind keine freien Termine verfuegbar.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th scope="col">Arzt</th>
                <th scope="col">Beginn</th>
                <th scope="col"></th>
              </tr>
            </thead>
            <tbody>
              {freieSlots.map((slot) => (
                <tr key={slot.id}>
                  <td>{slot.arzt}</td>
                  <td>{formatiere(slot.beginn)}</td>
                  <td>
                    <button type="button" onClick={() => setSlotId(slot.id)}>
                      Auswaehlen
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>

      <section>
        <h2>Termin buchen</h2>
        <form onSubmit={buchen}>
          <label htmlFor="slotId">Termin-ID</label>
          <input
            id="slotId"
            value={slotId}
            onChange={(ereignis) => setSlotId(ereignis.target.value)}
            required
          />
          <label htmlFor="patientenname">Patientenname</label>
          <input
            id="patientenname"
            value={patientenname}
            onChange={(ereignis) => setPatientenname(ereignis.target.value)}
            required
          />
          <label htmlFor="grund">Grund des Besuchs</label>
          <textarea
            id="grund"
            value={grund}
            onChange={(ereignis) => setGrund(ereignis.target.value)}
            required
            rows={3}
          />
          <button type="submit">Termin buchen</button>
        </form>
      </section>

      <section>
        <h2>Meine Termine</h2>
        {termine.length === 0 ? (
          <p>Es sind keine Termine gebucht.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th scope="col">Arzt</th>
                <th scope="col">Beginn</th>
                <th scope="col">Patient</th>
                <th scope="col">Grund</th>
                <th scope="col"></th>
              </tr>
            </thead>
            <tbody>
              {termine.map((termin) => (
                <tr key={termin.id}>
                  <td>{termin.arzt}</td>
                  <td>{formatiere(termin.beginn)}</td>
                  <td>{termin.patientenname}</td>
                  <td>{termin.grund}</td>
                  <td>
                    <button type="button" onClick={() => void stornieren(termin.id)}>
                      Stornieren
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>

      {meldung && <p role="status">{meldung}</p>}
      {fehler && <p role="alert">{fehler}</p>}
    </main>
  )
}

export default App

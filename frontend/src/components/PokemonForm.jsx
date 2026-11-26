import { useState } from 'react'
import Modal from './Modal'
import TypeBadge from './TypeBadge'
import { STATS, MAX_STAT, formatId, typeGradient } from '../utils/pokemon'
import '../css/PokemonForm.css'

const EMPTY_STATS = Object.fromEntries(STATS.map((s) => [s.key, 50]))

// Formulário de cadastro/edição. Os dados são enviados à API pelo onSubmit (App).
export default function PokemonForm({ pokemon, types, onSubmit, onClose }) {
  const isEdit = Boolean(pokemon)
  const [id, setId] = useState(pokemon ? String(pokemon.id) : '')
  const [name, setName] = useState(pokemon?.name ?? '')
  const [type1, setType1] = useState(pokemon?.types[0]?.name ?? '')
  const [type2, setType2] = useState(pokemon?.types[1]?.name ?? '')
  const [stats, setStats] = useState(pokemon ? { ...pokemon.stats } : EMPTY_STATS)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState(null)

  const selectedTypes = [type1, type2].filter(Boolean)
  const total = Object.values(stats).reduce((sum, v) => sum + (Number(v) || 0), 0)
  const fieldError = (field) => error?.fields?.[field]

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSaving(true)
    setError(null)
    try {
      await onSubmit({
        id: id === '' ? null : Number(id),
        name: name.trim(),
        stats: Object.fromEntries(Object.entries(stats).map(([k, v]) => [k, Number(v)])),
        types: selectedTypes.map((t) => ({ name: t })),
      })
    } catch (err) {
      setError({ message: err.message, fields: err.fields ?? {} })
      setSaving(false)
    }
  }

  return (
    <Modal onClose={onClose} labelledBy="form-title">
      <form className="poke-form" onSubmit={handleSubmit} noValidate={false}>
        <div
          className="poke-form__hero"
          style={{ background: selectedTypes.length ? typeGradient(selectedTypes.map((n) => ({ name: n }))) : undefined }}
        >
          <h2 id="form-title">{isEdit ? `✏️ Editar ${pokemon.name}` : '➕ Novo Pokémon'}</h2>
          <div className="poke-form__types">
            {selectedTypes.map((t) => (
              <TypeBadge key={t} type={t} />
            ))}
          </div>
        </div>

        <div className="poke-form__body">
          {error && (
            <p className="poke-form__alert" role="alert">
              ⚠️ {error.message}
            </p>
          )}

          <div className="poke-form__row">
            <label className="poke-form__field poke-form__field--id">
              Nº da Pokédex
              {isEdit ? (
                <input value={formatId(pokemon.id)} disabled />
              ) : (
                <input
                  type="number"
                  min="1"
                  placeholder="Automático"
                  value={id}
                  onChange={(e) => setId(e.target.value)}
                />
              )}
              {fieldError('id') && <small className="poke-form__error">{fieldError('id')}</small>}
            </label>

            <label className="poke-form__field poke-form__field--grow">
              Nome
              <input
                required
                maxLength={30}
                placeholder="Ex.: Pikachu"
                value={name}
                onChange={(e) => setName(e.target.value)}
                autoFocus
              />
              {fieldError('name') && <small className="poke-form__error">{fieldError('name')}</small>}
            </label>
          </div>

          <div className="poke-form__row">
            <label className="poke-form__field poke-form__field--grow">
              Tipo principal
              <select required value={type1} onChange={(e) => setType1(e.target.value)}>
                <option value="" disabled>
                  Selecione...
                </option>
                {types.map((t) => (
                  <option key={t} value={t} disabled={t === type2}>
                    {t}
                  </option>
                ))}
              </select>
            </label>
            <label className="poke-form__field poke-form__field--grow">
              Tipo secundário
              <select value={type2} onChange={(e) => setType2(e.target.value)}>
                <option value="">Nenhum</option>
                {types.map((t) => (
                  <option key={t} value={t} disabled={t === type1}>
                    {t}
                  </option>
                ))}
              </select>
            </label>
          </div>
          {fieldError('types') && <small className="poke-form__error">{fieldError('types')}</small>}

          <fieldset className="poke-form__stats">
            <legend>
              📊 Atributos <span>Total: {total}</span>
            </legend>
            {STATS.map((stat) => (
              <label key={stat.key} className="poke-form__stat">
                <span>
                  {stat.icon} {stat.label}
                </span>
                <input
                  type="range"
                  min="1"
                  max={MAX_STAT}
                  value={stats[stat.key] || 1}
                  onChange={(e) => setStats((s) => ({ ...s, [stat.key]: e.target.value }))}
                  aria-hidden="true"
                  tabIndex={-1}
                />
                <input
                  type="number"
                  required
                  min="1"
                  max={MAX_STAT}
                  value={stats[stat.key]}
                  onChange={(e) => setStats((s) => ({ ...s, [stat.key]: e.target.value }))}
                  aria-label={stat.label}
                />
                {fieldError(stat.key) && <small className="poke-form__error">{fieldError(stat.key)}</small>}
              </label>
            ))}
          </fieldset>

          <div className="poke-form__actions">
            <button type="button" className="btn" onClick={onClose} disabled={saving}>
              Cancelar
            </button>
            <button type="submit" className="btn btn--primary" disabled={saving}>
              {saving ? '⏳ Salvando...' : isEdit ? '💾 Salvar alterações' : '➕ Cadastrar'}
            </button>
          </div>
        </div>
      </form>
    </Modal>
  )
}

// Cliente da API REST (Spring Boot + MongoDB).
// Em desenvolvimento o Vite redireciona /api para http://localhost:8080 (ver vite.config.js).
// Para usar outro endereço, defina VITE_API_URL no arquivo .env (ex.: https://minha-api.com).
const BASE_URL = `${(import.meta.env.VITE_API_URL ?? '').replace(/\/$/, '')}/api`

export class ApiError extends Error {
  constructor(message, status, fields = {}) {
    super(message)
    this.status = status
    this.fields = fields
  }
}

async function request(path, options = {}) {
  let response
  try {
    response = await fetch(`${BASE_URL}${path}`, {
      ...options,
      headers: { 'Content-Type': 'application/json', ...options.headers },
    })
  } catch {
    throw new ApiError('Não foi possível conectar à API. O backend está rodando?', 0)
  }

  if (response.status === 204) return null

  const body = await response.json().catch(() => null)
  if (!response.ok) {
    throw new ApiError(body?.message ?? `Erro ${response.status} na API`, response.status, body?.fields)
  }
  return body
}

// O documento do MongoDB guarda os atributos no primeiro nível e os tipos pelo nome;
// a interface da Atividade 03 usa { stats: {...}, types: [{ id, name }] }.
export const STAT_KEYS = ['hp', 'attack', 'defense', 'specialAttack', 'specialDefense', 'speed']

export function fromApi(doc, typeIdByName = {}) {
  return {
    id: doc.id,
    name: doc.name,
    stats: Object.fromEntries(STAT_KEYS.map((key) => [key, doc[key]])),
    types: doc.types.map((name) => ({ id: typeIdByName[name] ?? name, name })),
  }
}

export function toApi(pokemon) {
  return {
    ...(pokemon.id != null && { id: pokemon.id }),
    name: pokemon.name,
    ...pokemon.stats,
    types: pokemon.types.map((t) => (typeof t === 'string' ? t : t.name)),
  }
}

export const fetchPokemons = () => request('/pokemons')
export const fetchTypes = () => request('/types')
export const fetchTrainers = () => request('/trainers')

export const createPokemon = (pokemon) =>
  request('/pokemons', { method: 'POST', body: JSON.stringify(toApi(pokemon)) })

export const updatePokemon = (id, pokemon) =>
  request(`/pokemons/${id}`, { method: 'PUT', body: JSON.stringify(toApi(pokemon)) })

export const deletePokemon = (id) => request(`/pokemons/${id}`, { method: 'DELETE' })

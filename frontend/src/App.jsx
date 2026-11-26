import { useCallback, useEffect, useMemo, useState } from 'react'
import Header from './components/Header'
import SearchBar from './components/SearchBar'
import Filters from './components/Filters'
import PokemonList from './components/PokemonList'
import PokemonDetails from './components/PokemonDetails'
import PokemonForm from './components/PokemonForm'
import ComparePanel, { CompareBar } from './components/ComparePanel'
import TrainerList from './components/TrainerList'
import PokedexIntro from './components/PokedexIntro'
import useLocalStorage from './hooks/useLocalStorage'
import useFavorites from './hooks/useFavorites'
import { totalStats } from './utils/pokemon'
import {
  fetchPokemons,
  fetchTypes,
  fetchTrainers,
  fromApi,
  createPokemon,
  updatePokemon,
  deletePokemon,
} from './services/api'
import './css/App.css'

const byId = (a, b) => a.id - b.id

export default function App() {
  // Dados vindos da API (Spring Boot + MongoDB)
  const [allPokemon, setAllPokemon] = useState([])
  const [types, setTypes] = useState([])
  const [trainers, setTrainers] = useState([])
  const [loadStatus, setLoadStatus] = useState('loading') // 'loading' | 'ready' | 'error'
  const [loadError, setLoadError] = useState('')

  const [showIntro, setShowIntro] = useState(true)
  const [view, setView] = useState('pokedex')
  const [search, setSearch] = useState('')
  const [selectedType, setSelectedType] = useState('All')
  const [sortBy, setSortBy] = useState('id')
  const [sortDir, setSortDir] = useState('asc')
  const [onlyFavorites, setOnlyFavorites] = useState(false)
  const [selectedId, setSelectedId] = useState(null)
  const [compareIds, setCompareIds] = useState([])
  const [showCompare, setShowCompare] = useState(false)
  const [editing, setEditing] = useState(null) // null | { pokemon: null (novo) | Pokémon }
  const { favorites, toggleFavorite, status: favoritesStatus } = useFavorites()
  const [theme, setTheme] = useLocalStorage(
    'pokedex:theme',
    window.matchMedia?.('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
  )

  useEffect(() => {
    document.documentElement.dataset.theme = theme
  }, [theme])

  const typeIdByName = useMemo(() => Object.fromEntries(types.map((t) => [t.name, t.id])), [types])

  const loadData = useCallback(() => {
    setLoadStatus('loading')
    Promise.all([fetchPokemons(), fetchTypes(), fetchTrainers()])
      .then(([pokemonDocs, typeDocs, trainerDocs]) => {
        const ids = Object.fromEntries(typeDocs.map((t) => [t.name, t.id]))
        setTypes(typeDocs)
        setAllPokemon(pokemonDocs.map((doc) => fromApi(doc, ids)))
        setTrainers(trainerDocs)
        setLoadStatus('ready')
      })
      .catch((err) => {
        setLoadError(err.message)
        setLoadStatus('error')
      })
  }, [])

  useEffect(() => {
    loadData()
  }, [loadData])

  const pokemonById = useMemo(() => Object.fromEntries(allPokemon.map((p) => [p.id, p])), [allPokemon])

  // Apenas os tipos que realmente aparecem nos Pokémon, na ordem da coleção "types"
  const availableTypes = useMemo(() => {
    const used = new Set(allPokemon.flatMap((p) => p.types.map((t) => t.name)))
    return types.map((t) => t.name).filter((name) => used.has(name))
  }, [allPokemon, types])

  // Estado derivado: lista filtrada e ordenada
  const visiblePokemon = useMemo(() => {
    const term = search.trim().toLowerCase().replace(/^#/, '')

    const filtered = allPokemon.filter((p) => {
      const matchesSearch =
        term === '' || p.name.toLowerCase().includes(term) || String(p.id) === String(Number(term))
      const matchesType = selectedType === 'All' || p.types.some((t) => t.name === selectedType)
      const matchesFavorite = !onlyFavorites || favorites.includes(p.id)
      return matchesSearch && matchesType && matchesFavorite
    })

    const getValue = (p) =>
      sortBy === 'id' ? p.id : sortBy === 'name' ? p.name : sortBy === 'total' ? totalStats(p) : p.stats[sortBy]

    const sorted = [...filtered].sort((a, b) => {
      const va = getValue(a)
      const vb = getValue(b)
      const result = typeof va === 'string' ? va.localeCompare(vb) : va - vb
      return result !== 0 ? result : a.id - b.id
    })

    return sortDir === 'asc' ? sorted : sorted.reverse()
  }, [allPokemon, search, selectedType, sortBy, sortDir, onlyFavorites, favorites])

  const toggleCompare = (id) =>
    setCompareIds((prev) => {
      if (prev.includes(id)) return prev.filter((c) => c !== id)
      return prev.length >= 2 ? [prev[1], id] : [...prev, id]
    })

  const resetFilters = () => {
    setSearch('')
    setSelectedType('All')
    setOnlyFavorites(false)
  }

  const selectRandom = () => {
    const pool = visiblePokemon.length > 0 ? visiblePokemon : allPokemon
    if (pool.length > 0) setSelectedId(pool[Math.floor(Math.random() * pool.length)].id)
  }

  // Navega para o anterior/próximo dentro da lista visível (ou de todos, se o Pokémon não estiver nela)
  const stepSelection = (direction) => {
    const list = visiblePokemon.some((p) => p.id === selectedId) ? visiblePokemon : allPokemon
    const index = list.findIndex((p) => p.id === selectedId)
    setSelectedId(list[(index + direction + list.length) % list.length].id)
  }

  // ---- Cadastro, edição e exclusão (persistidos no MongoDB pela API) ----
  const savePokemon = async (data) => {
    const original = editing.pokemon
    const saved = fromApi(original ? await updatePokemon(original.id, data) : await createPokemon(data), typeIdByName)

    setAllPokemon((prev) => [...prev.filter((p) => p.id !== saved.id), saved].sort(byId))
    setEditing(null)
    setSelectedId(saved.id)
  }

  const removePokemon = async (pokemon) => {
    await deletePokemon(pokemon.id)

    setAllPokemon((prev) => prev.filter((p) => p.id !== pokemon.id))
    // a API também remove o Pokémon das equipes dos treinadores
    setTrainers((prev) => prev.map((t) => ({ ...t, pokemonIds: t.pokemonIds.filter((id) => id !== pokemon.id) })))
    setCompareIds((prev) => prev.filter((id) => id !== pokemon.id))
    if (favorites.includes(pokemon.id)) toggleFavorite(pokemon.id)
    setSelectedId(null)
  }

  const closeDetails = useCallback(() => setSelectedId(null), [])
  const closeCompare = useCallback(() => setShowCompare(false), [])
  const closeForm = useCallback(() => setEditing(null), [])
  const finishIntro = useCallback(() => setShowIntro(false), [])

  const selectedPokemon = selectedId ? pokemonById[selectedId] : null
  const comparePokemon = compareIds.map((id) => pokemonById[id]).filter(Boolean)

  return (
    <div className="app">
      {/* A tela principal já é montada por baixo da intro, adiantando o carregamento dos dados e imagens */}
      {showIntro && <PokedexIntro total={allPokemon.length} onFinish={finishIntro} />}

      <Header
        view={view}
        onChangeView={setView}
        theme={theme}
        onToggleTheme={() => setTheme(theme === 'dark' ? 'light' : 'dark')}
        totals={{ pokemon: allPokemon.length, trainers: trainers.length }}
      />

      <main className="app__main">
        {loadStatus === 'loading' && (
          <div className="empty-state">
            <img src="/pokeball.svg" alt="" />
            <h2>Carregando a Pokédex...</h2>
            <p>Buscando os dados na API.</p>
          </div>
        )}

        {loadStatus === 'error' && (
          <div className="empty-state">
            <img src="/pokeball.svg" alt="" />
            <h2>Não foi possível carregar os Pokémon</h2>
            <p>{loadError}</p>
            <button className="btn btn--primary" onClick={loadData}>
              🔄 Tentar novamente
            </button>
          </div>
        )}

        {loadStatus === 'ready' &&
          (view === 'pokedex' ? (
            <>
              <SearchBar value={search} onChange={setSearch} />
              <Filters
                types={availableTypes}
                selectedType={selectedType}
                onSelectType={setSelectedType}
                sortBy={sortBy}
                onSortBy={setSortBy}
                sortDir={sortDir}
                onToggleSortDir={() => setSortDir((d) => (d === 'asc' ? 'desc' : 'asc'))}
                onlyFavorites={onlyFavorites}
                onToggleFavorites={() => setOnlyFavorites((v) => !v)}
                favoritesCount={favorites.filter((id) => pokemonById[id]).length}
                favoritesStatus={favoritesStatus}
                onRandom={selectRandom}
                onCreate={() => setEditing({ pokemon: null })}
              />
              <PokemonList
                pokemon={visiblePokemon}
                favorites={favorites}
                compareIds={compareIds}
                onSelect={setSelectedId}
                onToggleFavorite={toggleFavorite}
                onToggleCompare={toggleCompare}
                onReset={resetFilters}
              />
            </>
          ) : (
            <TrainerList trainers={trainers} pokemonById={pokemonById} onSelectPokemon={setSelectedId} />
          ))}
      </main>

      <footer className="app__footer">
        Dados: API Spring Boot + MongoDB · Imagens e descrições: PokéAPI
      </footer>

      <CompareBar selected={comparePokemon} onOpen={() => setShowCompare(true)} onClear={() => setCompareIds([])} />

      {selectedPokemon && !editing && (
        <PokemonDetails
          pokemon={selectedPokemon}
          trainers={trainers.filter((t) => t.pokemonIds.includes(selectedPokemon.id))}
          isFavorite={favorites.includes(selectedPokemon.id)}
          onToggleFavorite={toggleFavorite}
          onEdit={() => setEditing({ pokemon: selectedPokemon })}
          onDelete={removePokemon}
          onClose={closeDetails}
          onPrev={() => stepSelection(-1)}
          onNext={() => stepSelection(1)}
        />
      )}

      {editing && (
        <PokemonForm
          pokemon={editing.pokemon}
          types={types.map((t) => t.name)}
          onSubmit={savePokemon}
          onClose={closeForm}
        />
      )}

      {showCompare && comparePokemon.length === 2 && <ComparePanel pair={comparePokemon} onClose={closeCompare} />}
    </div>
  )
}

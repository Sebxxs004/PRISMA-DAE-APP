import React, { createContext, useContext, useState, useEffect } from 'react';
import axios from 'axios';

const GlobalContext = createContext();

export function GlobalProvider({ children }) {
  const [casos, setCasos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  // Array of groups: { id, name, cases: [], type, justification }
  const [groups, setGroups] = useState([]);

  useEffect(() => {
    const fetchCasos = async () => {
      try {
        const response = await axios.get('http://localhost:8080/api/casos');
        const uniqueData = Array.from(new Map(response.data.map(item => [item.nombre, item])).values());
        setCasos(uniqueData);
      } catch (err) {
        console.error("Error al cargar casos:", err);
        setError("No se pudo conectar con el servidor.");
        
        // Mock data to show the layout even if backend fails
        setCasos(Array.from({ length: 20 }, (_, i) => ({
          nombre: `1100160000992023${i}000`,
          radicado: `1100160000992023${i}000`,
          delitos: [i % 2 === 0 ? 'hurto calificado' : 'extorsion'],
          descripcion: 'Descripción mock'
        })));
      } finally {
        setLoading(false);
      }
    };

    fetchCasos();
  }, []);

  const addGroup = (newGroup) => {
    setGroups(prev => [...prev, newGroup]);
  };

  const addCasesToGroup = (groupId, casesToAdd) => {
    setGroups(prev => prev.map(g => {
      if (g.id === groupId) {
        return { ...g, cases: [...new Set([...g.cases, ...casesToAdd])] };
      }
      return g;
    }));
  };

  const getCaseGroup = (casoNombre) => {
    return groups.find(g => g.cases.includes(casoNombre));
  };

  const updateGroupDecision = (groupId, decisionData) => {
    setGroups(prev => prev.map(g => g.id === groupId ? { ...g, decision: decisionData } : g));
  };

  return (
    <GlobalContext.Provider value={{
      casos, loading, error, 
      groups, addGroup, addCasesToGroup, getCaseGroup, updateGroupDecision
    }}>
      {children}
    </GlobalContext.Provider>
  );
}

export function useGlobal() {
  return useContext(GlobalContext);
}

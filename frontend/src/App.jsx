import { Routes, Route } from 'react-router-dom';
import { GlobalProvider } from './context/GlobalContext';
import Login from './pages/Login';
import Inicio from './pages/Inicio';
import CaseSelection from './pages/CaseSelection';
import Board from './pages/Board';

function App() {
  return (
    <GlobalProvider>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/inicio" element={<Inicio />} />
        <Route path="/cases" element={<CaseSelection />} />
        <Route path="/board/:caseId" element={<Board />} />
      </Routes>
    </GlobalProvider>
  );
}

export default App;

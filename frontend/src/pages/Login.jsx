import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { FaSignInAlt, FaBookOpen, FaPowerOff, FaUsers } from 'react-icons/fa';

export default function Login() {
  const navigate = useNavigate();
  const [showModal, setShowModal] = useState(false);
  const [member1, setMember1] = useState('');
  const [member2, setMember2] = useState('');
  const [member3, setMember3] = useState('');

  const handleStart = () => {
    setShowModal(true);
  };

  const handleLogin = (e) => {
    e.preventDefault();
    if (member1.trim() && member2.trim() && member3.trim()) {
      const team = `${member1}, ${member2}, ${member3}`;
      localStorage.setItem('nexus_user', team);
      // Start global timer
      localStorage.setItem('nexus_timer_start', Date.now().toString());
      navigate('/inicio');
    } else {
      alert("Por favor ingrese los nombres de los 3 miembros del grupo.");
    }
  };

  return (
    <div style={{ 
      backgroundImage: "url('/assets/fondo-login.png')", 
      backgroundSize: 'cover',
      backgroundPosition: 'center',
      height: '100vh',
      width: '100vw',
      display: 'flex',
      alignItems: 'center',
      paddingLeft: '5rem',
      position: 'relative'
    }}>
      
      <div style={{ maxWidth: '400px', color: 'white', zIndex: 10 }}>
        {/* Top logo area */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '20px' }}>
          <div style={{ backgroundColor: '#F59E0B', color: 'black', padding: '5px 8px', fontWeight: 'bold', borderRadius: '4px', fontSize: '14px' }}>PGN</div>
          <div>
            <div style={{ fontWeight: 'bold', fontSize: '14px' }}>NEXUS DAE</div>
            <div style={{ fontSize: '10px' }}>FISCALÍA GENERAL DE LA NACIÓN</div>
          </div>
        </div>

        {/* Status */}
        <div style={{ 
          display: 'inline-flex', 
          alignItems: 'center', 
          gap: '8px', 
          backgroundColor: 'rgba(255,255,255,0.1)', 
          padding: '5px 12px', 
          borderRadius: '20px',
          border: '1px solid rgba(255,255,255,0.2)',
          fontSize: '12px',
          marginBottom: '30px',
          backdropFilter: 'blur(5px)'
        }}>
          <div style={{ width: '8px', height: '8px', backgroundColor: '#10B981', borderRadius: '50%' }}></div>
          Sistema activo
        </div>

        {/* Titles */}
        <h1 style={{ fontSize: '3rem', fontWeight: '800', lineHeight: '1.1', marginBottom: '20px' }}>
          Actividad de <br/>
          simulación <br/>
          interactiva <br/>
          de Despacho <br/>
          <span style={{ color: '#F59E0B' }}>Fiscal</span>
        </h1>
        <p style={{ fontSize: '14px', marginBottom: '40px', opacity: 0.9, lineHeight: '1.5' }}>
          Acceso para fiscales autorizados. Asuma el rol de Fiscal Delegado y tome decisiones reales.
        </p>

        {/* Buttons */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
          
          <button onClick={handleStart} style={{ 
            backgroundColor: '#FDE047', 
            color: 'black', 
            border: 'none',
            borderRadius: '8px',
            padding: '15px 20px',
            display: 'flex',
            alignItems: 'center',
            gap: '15px',
            cursor: 'pointer',
            textAlign: 'left',
            transition: 'transform 0.2s',
            boxShadow: '0 4px 6px rgba(0,0,0,0.1)'
          }}
          onMouseOver={(e) => e.currentTarget.style.transform = 'scale(1.02)'}
          onMouseOut={(e) => e.currentTarget.style.transform = 'scale(1)'}
          >
            <FaSignInAlt size={24} color="rgba(0,0,0,0.5)" />
            <div>
              <div style={{ fontWeight: 'bold', fontSize: '16px' }}>Ingresar a NEXUS</div>
              <div style={{ fontSize: '12px', opacity: 0.8 }}>Iniciar despacho fiscal</div>
            </div>
          </button>

          <button style={{ 
            backgroundColor: 'rgba(255,255,255,0.1)', 
            color: 'white', 
            border: '1px solid rgba(255,255,255,0.3)',
            backdropFilter: 'blur(5px)',
            borderRadius: '8px',
            padding: '15px 20px',
            display: 'flex',
            alignItems: 'center',
            gap: '15px',
            cursor: 'pointer',
            textAlign: 'left',
            transition: 'all 0.2s'
          }}
          onMouseOver={(e) => { e.currentTarget.style.transform = 'scale(1.02)'; e.currentTarget.style.backgroundColor = 'rgba(255,255,255,0.2)'; }}
          onMouseOut={(e) => { e.currentTarget.style.transform = 'scale(1)'; e.currentTarget.style.backgroundColor = 'rgba(255,255,255,0.1)'; }}
          >
            <FaBookOpen size={24} color="white" />
            <div>
              <div style={{ fontWeight: 'bold', fontSize: '16px' }}>Leer instrucciones</div>
              <div style={{ fontSize: '12px', opacity: 0.7 }}>Cómo funciona el simulador</div>
            </div>
          </button>

          <button style={{ 
            backgroundColor: 'rgba(239, 68, 68, 0.1)', 
            color: '#EF4444', 
            border: '1px solid rgba(239, 68, 68, 0.5)',
            backdropFilter: 'blur(5px)',
            borderRadius: '8px',
            padding: '15px 20px',
            display: 'flex',
            alignItems: 'center',
            gap: '15px',
            cursor: 'pointer',
            textAlign: 'left',
            transition: 'all 0.2s'
          }}
          onMouseOver={(e) => { e.currentTarget.style.transform = 'scale(1.02)'; e.currentTarget.style.backgroundColor = 'rgba(239, 68, 68, 0.2)'; }}
          onMouseOut={(e) => { e.currentTarget.style.transform = 'scale(1)'; e.currentTarget.style.backgroundColor = 'rgba(239, 68, 68, 0.1)'; }}
          >
            <FaPowerOff size={24} color="#EF4444" />
            <div>
              <div style={{ fontWeight: 'bold', fontSize: '16px' }}>Salir del sistema</div>
              <div style={{ fontSize: '12px', opacity: 0.7 }}>Cerrar la aplicación</div>
            </div>
          </button>

        </div>
      </div>

      {/* MODAL FOR 3 MEMBERS */}
      <AnimatePresence>
        {showModal && (
          <motion.div 
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            style={{
              position: 'absolute',
              top: 0, left: 0, width: '100%', height: '100%',
              backgroundColor: 'rgba(0,0,0,0.8)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              zIndex: 100
            }}
          >
            <motion.div 
              initial={{ scale: 0.9, y: 50 }}
              animate={{ scale: 1, y: 0 }}
              exit={{ scale: 0.9, y: 50 }}
              style={{
                backgroundColor: '#1E293B',
                padding: '40px',
                borderRadius: '16px',
                width: '100%',
                maxWidth: '450px',
                border: '1px solid #334155',
                boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.5)'
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                <h2 style={{ margin: 0, color: 'white', display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <FaUsers color="#F59E0B" /> Registro de Grupo
                </h2>
                <button onClick={() => setShowModal(false)} style={{ background: 'none', border: 'none', color: '#94A3B8', fontSize: '20px', cursor: 'pointer' }}>✖</button>
              </div>

              <p style={{ color: '#94A3B8', marginBottom: '20px', fontSize: '14px' }}>Ingrese los nombres de los tres investigadores asignados a este despacho.</p>

              <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                <input 
                  type="text" 
                  placeholder="Investigador 1" 
                  value={member1} onChange={(e) => setMember1(e.target.value)}
                  style={{ padding: '12px', borderRadius: '8px', border: '1px solid #334155', backgroundColor: '#0F172A', color: 'white', outline: 'none' }}
                  required
                />
                <input 
                  type="text" 
                  placeholder="Investigador 2" 
                  value={member2} onChange={(e) => setMember2(e.target.value)}
                  style={{ padding: '12px', borderRadius: '8px', border: '1px solid #334155', backgroundColor: '#0F172A', color: 'white', outline: 'none' }}
                  required
                />
                <input 
                  type="text" 
                  placeholder="Investigador 3" 
                  value={member3} onChange={(e) => setMember3(e.target.value)}
                  style={{ padding: '12px', borderRadius: '8px', border: '1px solid #334155', backgroundColor: '#0F172A', color: 'white', outline: 'none' }}
                  required
                />
                
                <button 
                  type="submit" 
                  style={{ 
                    marginTop: '10px', padding: '15px', backgroundColor: '#FDE047', color: 'black', 
                    fontWeight: 'bold', border: 'none', borderRadius: '8px', cursor: 'pointer', transition: 'background-color 0.2s' 
                  }}
                  onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#FACC15'}
                  onMouseOut={(e) => e.currentTarget.style.backgroundColor = '#FDE047'}
                >
                  INICIAR SIMULACIÓN
                </button>
              </form>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

    </div>
  );
}

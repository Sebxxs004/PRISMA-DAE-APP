import { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { FaArrowLeft, FaFolderOpen, FaChartBar, FaClock, FaBriefcase, FaUserGraduate } from 'react-icons/fa';
import { useTimer } from '../utils/useTimer';

export default function Inicio() {
  const navigate = useNavigate();
  const [isPlayingTransition, setIsPlayingTransition] = useState(false);
  const videoRef = useRef(null);
  
  const { timeLeft, formatTime } = useTimer();

  const handleGestionesClick = () => {
    setIsPlayingTransition(true);
    if (videoRef.current) {
      videoRef.current.play();
    }
  };

  const handleVideoEnd = () => {
    setIsPlayingTransition(false);
    navigate('/cases');
  };

  return (
    <div className="inicio-bg" style={{ position: 'relative' }}>
      
      {/* Video Transition Overlay */}
      <AnimatePresence>
        {isPlayingTransition && (
          <motion.div 
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            style={{
              position: 'fixed',
              top: 0, left: 0, width: '100vw', height: '100vh',
              backgroundColor: 'black',
              zIndex: 9999,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center'
            }}
          >
            <video 
              ref={videoRef}
              src="/videos/VIDEO2.mp4" 
              autoPlay 
              onEnded={handleVideoEnd}
              style={{ width: '100%', height: '100%', objectFit: 'cover' }}
            />
          </motion.div>
        )}
      </AnimatePresence>

      {/* Top Left: Volver al login */}
      <button 
        onClick={() => navigate('/')}
        style={{
          position: 'absolute',
          top: '20px',
          left: '20px',
          backgroundColor: 'rgba(0,0,0,0.6)',
          color: 'white',
          border: '1px solid rgba(255,255,255,0.2)',
          padding: '8px 15px',
          borderRadius: '8px',
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          cursor: 'pointer',
          backdropFilter: 'blur(5px)',
          fontWeight: 'bold',
          zIndex: 10
        }}
      >
        <FaArrowLeft /> Volver al login
      </button>

      {/* Top Right: Tiempo */}
      <div style={{
        position: 'absolute',
        top: '20px',
        right: '20px',
        backgroundColor: '#EF4444',
        color: 'white',
        padding: '8px 15px',
        borderRadius: '20px',
        display: 'flex',
        alignItems: 'center',
        gap: '8px',
        fontWeight: 'bold',
        fontSize: '14px',
        boxShadow: '0 4px 6px rgba(0,0,0,0.3)',
        zIndex: 10
      }}>
        <div style={{ width: '8px', height: '8px', backgroundColor: 'white', borderRadius: '50%' }}></div>
        TIEMPO {formatTime(timeLeft)}
      </div>

      {/* Top Center: Progress Bar (Mock) */}
      <div style={{
        position: 'absolute',
        top: '25px',
        left: '50%',
        transform: 'translateX(-50%)',
        width: '200px',
        height: '10px',
        backgroundColor: 'rgba(255,255,255,0.3)',
        border: '1px solid white',
        borderRadius: '5px',
        overflow: 'hidden',
        zIndex: 10
      }}>
        <div style={{ 
          width: `${(timeLeft / (3 * 60 * 60 * 1000)) * 100}%`, 
          height: '100%', 
          backgroundColor: '#EF4444',
          transition: 'width 1s linear'
        }}></div>
      </div>

      {/* Center Content */}
      <div style={{
        position: 'absolute',
        top: '30%',
        left: '50%',
        transform: 'translate(-50%, -50%)',
        textAlign: 'center',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        width: '100%',
        zIndex: 10
      }}>
        <h1 style={{ 
          color: 'white', 
          fontSize: '2.5rem', 
          fontWeight: 'bold',
          textShadow: '0 2px 10px rgba(0,0,0,0.8)',
          marginBottom: '20px'
        }}>
          Bienvenido a tu Despacho
        </h1>
        
        <img src="/assets/PRISMA-DAE.png" alt="Brain Logo" style={{ width: '80px', filter: 'drop-shadow(0 0 10px rgba(255,255,255,0.8))' }} />
        
        <div style={{ color: 'white', marginTop: '10px', textShadow: '0 2px 4px rgba(0,0,0,0.8)' }}>
          <div style={{ fontWeight: 'bold', fontSize: '1.2rem', letterSpacing: '2px' }}>NEXUS</div>
          <div style={{ fontSize: '0.7rem', opacity: 0.8, letterSpacing: '1px' }}>DIRECCIÓN DE ALTOS ESTUDIOS</div>
        </div>

        {/* 3 Pills */}
        <div style={{ display: 'flex', gap: '15px', marginTop: '20px' }}>
          <div style={{ backgroundColor: 'rgba(255,255,255,0.1)', border: '1px solid rgba(255,255,255,0.2)', padding: '5px 15px', borderRadius: '20px', display: 'flex', alignItems: 'center', gap: '5px', color: 'white', fontSize: '0.85rem', backdropFilter: 'blur(5px)' }}>
            <FaClock color="#FBBF24" /> Jornada: 3 horas
          </div>
          <div style={{ backgroundColor: 'rgba(255,255,255,0.1)', border: '1px solid rgba(255,255,255,0.2)', padding: '5px 15px', borderRadius: '20px', display: 'flex', alignItems: 'center', gap: '5px', color: 'white', fontSize: '0.85rem', backdropFilter: 'blur(5px)' }}>
            <FaBriefcase color="#FBBF24" /> Casos activos: 65
          </div>
          <div style={{ backgroundColor: 'rgba(255,255,255,0.1)', border: '1px solid rgba(255,255,255,0.2)', padding: '5px 15px', borderRadius: '20px', display: 'flex', alignItems: 'center', gap: '5px', color: 'white', fontSize: '0.85rem', backdropFilter: 'blur(5px)' }}>
            <FaUserGraduate color="#FBBF24" /> Equipo: 1 Judicante
          </div>
        </div>
      </div>

      {/* Bottom Buttons */}
      <motion.div 
        initial={{ opacity: 0, y: 50 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.8 }}
        style={{
          position: 'absolute',
          bottom: '40px',
          width: '100%',
          display: 'flex',
          justifyContent: 'space-between',
          padding: '0 40px',
          zIndex: 10
        }}
      >
        {/* Left Button */}
        <button 
          onClick={handleGestionesClick}
          style={{
            backgroundColor: '#FDE047',
            color: 'black',
            padding: '12px 20px',
            border: 'none',
            borderRadius: '8px',
            boxShadow: '0 4px 15px rgba(0,0,0,0.5)',
            display: 'flex',
            alignItems: 'center',
            gap: '15px',
            cursor: 'pointer',
            textAlign: 'left',
            minWidth: '250px',
            transition: 'transform 0.2s'
          }}
          onMouseOver={(e) => e.currentTarget.style.transform = 'scale(1.02)'}
          onMouseOut={(e) => e.currentTarget.style.transform = 'scale(1)'}
        >
          <FaFolderOpen size={24} />
          <div>
            <div style={{ fontWeight: 'bold', fontSize: '14px' }}>Procesos del despacho</div>
            <div style={{ fontSize: '10px', opacity: 0.7 }}>Noticias criminales y expedientes</div>
          </div>
        </button>

        {/* Right Button */}
        <button 
          onClick={() => navigate('/board/demo')} // Later change to pick case
          style={{
            backgroundColor: '#FDE047',
            color: 'black',
            padding: '12px 20px',
            border: 'none',
            borderRadius: '8px',
            boxShadow: '0 4px 15px rgba(0,0,0,0.5)',
            display: 'flex',
            alignItems: 'center',
            gap: '15px',
            cursor: 'pointer',
            textAlign: 'left',
            minWidth: '250px',
            transition: 'transform 0.2s'
          }}
          onMouseOver={(e) => e.currentTarget.style.transform = 'scale(1.02)'}
          onMouseOut={(e) => e.currentTarget.style.transform = 'scale(1)'}
        >
          <div>
            <div style={{ fontWeight: 'bold', fontSize: '14px' }}>Toma de decisiones</div>
            <div style={{ fontSize: '10px', opacity: 0.7 }}>Patrones y conexiones</div>
          </div>
          <FaChartBar size={24} />
        </button>
      </motion.div>
    </div>
  );
}

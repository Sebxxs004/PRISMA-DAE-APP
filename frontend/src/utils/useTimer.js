import { useState, useEffect } from 'react';

export function useTimer() {
  const [timeLeft, setTimeLeft] = useState(0);
  const [isFinished, setIsFinished] = useState(false);

  useEffect(() => {
    const startTimeStr = localStorage.getItem('nexus_timer_start');
    if (!startTimeStr) {
      return;
    }

    const startTime = parseInt(startTimeStr, 10);
    // 3 hours in milliseconds
    const duration = 3 * 60 * 60 * 1000; 

    const updateTimer = () => {
      const now = Date.now();
      const elapsed = now - startTime;
      const remaining = Math.max(0, duration - elapsed);
      
      setTimeLeft(remaining);
      
      if (remaining === 0 && !isFinished) {
        setIsFinished(true);
        // Dispatch an event or handle finish (e.g. show report)
        window.dispatchEvent(new Event('nexus_timer_finished'));
      }
    };

    updateTimer();
    const interval = setInterval(updateTimer, 1000);
    return () => clearInterval(interval);
  }, [isFinished]);

  const formatTime = (ms) => {
    if (ms <= 0) return "00:00:00";
    const totalSeconds = Math.floor(ms / 1000);
    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;
    return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
  };

  return { timeLeft, formatTime, isFinished };
}

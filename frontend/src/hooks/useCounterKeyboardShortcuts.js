import { useEffect } from 'react';

export const useCounterKeyboardShortcuts = ({
  onCallNext,
  onOpenCompleteModal,
  onOpenSkipModal,
  onTogglePause,
  isModalOpen,
  isPaused,
  hasCurrentCustomer,
}) => {
  useEffect(() => {
    const handleKeyDown = (e) => {
      // Ignore keybindings if user is typing in an input or select or modal is open
      if (
        isModalOpen ||
        ['INPUT', 'TEXTAREA', 'SELECT'].includes(e.target.tagName)
      ) {
        return;
      }

      const key = e.key.toLowerCase();

      if (key === 'n' && !isPaused) {
        e.preventDefault();
        onCallNext();
      } else if (key === 'c' && hasCurrentCustomer && !isPaused) {
        e.preventDefault();
        onOpenCompleteModal();
      } else if (key === 's' && hasCurrentCustomer && !isPaused) {
        e.preventDefault();
        onOpenSkipModal();
      } else if (key === 'p') {
        e.preventDefault();
        onTogglePause();
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [
    onCallNext,
    onOpenCompleteModal,
    onOpenSkipModal,
    onTogglePause,
    isModalOpen,
    isPaused,
    hasCurrentCustomer,
  ]);
};

import React from 'react';

export const AriaAnnouncer = ({ message }) => {
  return (
    <div
      aria-live="polite"
      aria-atomic="true"
      className="sr-only"
      id="aria-live-announcer"
    >
      {message}
    </div>
  );
};

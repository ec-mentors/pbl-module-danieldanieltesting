import { useState, useEffect } from 'react';

/**
 * A custom React hook that debounces a value.
 * @param {any} value The value to debounce.
 * @param {number} delay The debounce delay in milliseconds.
 * @returns {any} The debounced value.
 */
export function useDebounce(value, delay) {
  // State to store the debounced value
  const [debouncedValue, setDebouncedValue] = useState(value);

  useEffect(
    () => {
      // Set up a timer to update the debounced value after the specified delay
      const handler = setTimeout(() => {
        setDebouncedValue(value);
      }, delay);

      // Clean up the timer if the value or delay changes before the timer fires.
      // This is the core of the debounce logic.
      return () => {
        clearTimeout(handler);
      };
    },
    [value, delay] // Only re-run the effect if value or delay changes
  );

  return debouncedValue;
}
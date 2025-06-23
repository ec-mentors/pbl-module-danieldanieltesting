import React from 'react';
import { format, formatDistanceToNow } from 'date-fns';

/**
 * A reusable component to display a human-readable, relative timestamp
 * (e.g., "5 minutes ago"). It also shows the full, localized date and
 * time in a tooltip on hover.
 *
 * @param {object} props
 * @param {string} props.dateString The ISO 8601 UTC date string from the backend API.
 */
const TimeAgo = ({ dateString }) => {
  if (!dateString) {
    return null; // Don't render anything if the date is not provided
  }

  let timeAgo = '';
  let fullDate = '';

  try {
    const date = new Date(dateString);

    // Calculate the relative time (e.g., "about 5 hours ago")
    // The 'addSuffix: true' adds the "ago" or "from now" part.
    timeAgo = formatDistanceToNow(date, { addSuffix: true });

    // Format the absolute date for the tooltip (e.g., "June 22, 2025, 9:56:32 PM")
    // 'PPPpp' is a date-fns token for a long, localized date and time with seconds.
    fullDate = format(date, 'PPPpp');

  } catch (error) {
    // If the date string is invalid, just display it as is to help with debugging.
    console.error("Invalid date string provided to TimeAgo component:", dateString);
    return <span>{dateString}</span>;
  }

  // The `title` attribute creates a native browser tooltip on hover.
  return (
    <span title={fullDate}>
      {timeAgo}
    </span>
  );
};

export default TimeAgo;
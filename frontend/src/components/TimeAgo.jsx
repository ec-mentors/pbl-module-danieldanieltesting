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
    return null; 
  }

  let timeAgo = '';
  let fullDate = '';

  try {
    const date = new Date(dateString);

    timeAgo = formatDistanceToNow(date, { addSuffix: true });

    fullDate = format(date, 'PPPpp');

  } catch (error) {

    console.error("Invalid date string provided to TimeAgo component:", dateString);
    return <span>{dateString}</span>;
  }

  return (
    <span title={fullDate}>
      {timeAgo}
    </span>
  );
};

export default TimeAgo;
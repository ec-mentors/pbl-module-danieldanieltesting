import React from 'react';

const Spinner = ({ message = 'Loading...' }) => {
  return (
    <div className="flex flex-col justify-center items-center py-20">
      <div className="flex space-x-2">
        <div className="w-4 h-4 bg-blue-500 rounded-full animate-pulse [animation-delay:-0.3s]"></div>
        <div className="w-4 h-4 bg-blue-500 rounded-full animate-pulse [animation-delay:-0.15s]"></div>
        <div className="w-4 h-4 bg-blue-500 rounded-full animate-pulse"></div>
      </div>
      <p className="mt-4 text-gray-500">{message}</p>
    </div>
  );
};

export default Spinner;
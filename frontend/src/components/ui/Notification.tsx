import React, { useEffect } from 'react';
import { CheckCircle, XCircle, X } from 'lucide-react';
import { cn } from '../../utils/cn';

export interface NotificationProps {
  type: 'success' | 'error';
  message: string;
  onClose: () => void;
}

const Notification: React.FC<NotificationProps> = ({ type, message, onClose }) => {
  useEffect(() => {
    const timer = setTimeout(() => {
      onClose();
    }, 1500);

    return () => clearTimeout(timer);
  }, [onClose]);

  return (
    <div
      className="fixed top-4 left-0 right-0 mx-auto max-w-md w-full bg-white rounded-lg shadow-lg p-4 flex items-center gap-3 z-50"
      style={{ 
        borderColor: type === 'success' ? '#22c55e' : '#ef4444',
        borderWidth: '1px'
      }}
    >
      {type === 'success' ? (
        <CheckCircle className="w-5 h-5 text-green-500" />
      ) : (
        <XCircle className="w-5 h-5 text-red-500" />
      )}
      <p className="flex-1 text-gray-900">
        {message}
      </p>
      <button
        onClick={onClose}
        className="text-gray-400 hover:text-gray-600"
      >
        <X className="w-5 h-5" />
      </button>
    </div>
  );
};

export default Notification; 
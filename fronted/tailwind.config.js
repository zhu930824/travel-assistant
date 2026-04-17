/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#eef2ff',
          100: '#e0e7ff',
          200: '#c7d2fe',
          300: '#a5b4fc',
          400: '#818cf8',
          500: '#6366f1',
          600: '#4f46e5',
          700: '#4338ca',
          800: '#3730a3',
          900: '#312e81',
        },
        aurora: {
          purple: '#7c3aed',
          pink: '#d946ef',
          rose: '#ec4899',
          amber: '#f59e0b',
          cyan: '#06b6d4',
        }
      },
      animation: {
        'gradient': 'gradient 15s ease infinite',
        'pulse-slow': 'pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'float': 'float 6s ease-in-out infinite',
        'aurora': 'aurora-shift 20s ease-in-out infinite alternate',
        'shimmer': 'shimmer 2s linear infinite',
      },
      keyframes: {
        gradient: {
          '0%, 100%': { backgroundPosition: '0% 50%' },
          '50%': { backgroundPosition: '100% 50%' },
        },
        float: {
          '0%, 100%': { transform: 'translateY(0px)' },
          '50%': { transform: 'translateY(-20px)' },
        },
        'aurora-shift': {
          '0%': { transform: 'scale(1) rotate(0deg)', opacity: '1' },
          '33%': { transform: 'scale(1.1) rotate(2deg)', opacity: '0.8' },
          '66%': { transform: 'scale(0.95) rotate(-1deg)', opacity: '0.9' },
          '100%': { transform: 'scale(1.05) rotate(1deg)', opacity: '1' },
        },
        shimmer: {
          '0%': { backgroundPosition: '-200% 0' },
          '100%': { backgroundPosition: '200% 0' },
        }
      },
      backdropBlur: {
        xs: '2px',
      },
      boxShadow: {
        'glow': '0 0 40px rgba(99, 102, 241, 0.3)',
        'glow-lg': '0 0 60px rgba(99, 102, 241, 0.4)',
        'glow-amber': '0 0 40px rgba(245, 158, 11, 0.3)',
      }
    },
  },
  plugins: [],
}

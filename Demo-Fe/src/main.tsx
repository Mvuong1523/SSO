import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { Provider } from 'react-redux'
import './index.css'
import AuthApp from './AuthApp.tsx'
import store from './store/store.ts'

createRoot(document.getElementById('root')!).render(
  <Provider store={store}>
    <AuthApp />
  </Provider>,
)

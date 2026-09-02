import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './app/App.tsx'
import { BrowserRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { store } from './app/store.ts'
import { Provider } from 'react-redux'
import AuthListener from './entities/session/ui/AuthListener.tsx'

const queryClient = new QueryClient();

// Provider store: React 앱 전체에 Redux store를 주입한다.
// AuthListener: 앱이 켜지자 마자 Supabase의 인증 상태 변화를 감시하다, 
//    로그인 세션이 확인되면 dispatch(setUser(...)) 를 실행해 Redux store의 빈자리를 채워준다.
createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <Provider store={store}>
      <AuthListener />
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <App />
        </BrowserRouter>
      </QueryClientProvider>
    </Provider>
  </StrictMode>,
)

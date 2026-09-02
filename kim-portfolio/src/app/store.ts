import { configureStore } from "@reduxjs/toolkit";
import authReducer from '../entities/session/model/authSlice';

// 앱의 중앙 저장소(store)를 만들어 authSlice의 리듀서를 auth 키로 등록
export const store = configureStore({
    reducer: {
        auth: authReducer
    }
})

// store에 등록된 리듀서들을 바탕으로 전체 상태 트리 타입(RootState)을 자동으로 추론해 뽑는다.
// 새 슬라이스를 추가해도 store.ts만 수정하면 타입이 자동으로 업데이트 된다.
export type RootState = ReturnType<typeof store.getState>

// store의 dispatch 함수 타입을 정의한다.
export type AppDispatch = typeof store.dispatch
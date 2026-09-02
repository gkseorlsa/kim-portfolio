
import { useDispatch, useSelector } from "react-redux"
import type { AppDispatch, RootState } from "./store"

// 매번 컴포넌트에서 useSelector((state: RootState) => ...) 처럼 일일히 타입을 명시하는
// 번거로움을 해소하기 위해, 타입이 미리 주입된 전용 훅을 만든다.
export const useAppDispatch = useDispatch.withTypes<AppDispatch>()
export const useAppSelector = useSelector.withTypes<RootState>()
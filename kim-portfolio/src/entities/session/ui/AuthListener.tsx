import { useEffect } from "react";
import { useAppDispatch } from "../../../app/hooks";
import { supabase } from "../../../shared/api/supabase";
import { setUser } from "../model/authSlice";

function AuthListener() {

    const dispatch = useAppDispatch()

    useEffect(() => {
        // 1. 앱이 켜질 때 한 번, 로컬 스토리지 등에 저장되어 있던 기존 세션을 꺼내온다.
        // pickUser: session 객체 전체에서 가벼운 유저 정보(id, email)만 골라내는 매퍼 함수 역할
        supabase.auth.getSession()
        .then(({ data }) => {
            dispatch(
                setUser(pickUser(data.session))
            )
        })

        // 2. 이후 변화를 실시간으로 구독하여, 로그인/로그아웃/토큰 갱신 등의 이벤트를 감시한다.
        // 만약 감지된다면 Redux를 동기화한다.
        const { data: listener } = supabase.auth.onAuthStateChange(
            (_event, session) =>
                dispatch(setUser(pickUser(session)))
        )

        // 3. 정리(Clean-up): 컴포넌트가 언마운트되면 웹소켓/이벤트 구독을 해제 하여, 불필요한 메모리 누수를 방지한다.
        return () =>
            listener.subscription.unsubscribe()
    }, [dispatch])

    return null     // 화면에는 아무것도 렌더링하지 않음으로써, 관리자 역할만 수행하도록 한다.
}

export default AuthListener;
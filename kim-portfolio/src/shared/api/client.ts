import { getToken } from "./token";

// 백엔드 서버 주소를 가져온다.
const BASE_URL = import.meta.env.VITE_API_BASE_URL

// 백엔드 구현 이전 (Supabase)

// 제네릭 사용: 호출하는 곳에서 응답받을 데이터의 타입을 직접 지정
// 경로(path): 도메인 뒤의 엔드포인트 경로(/users 또는 /posts/1)만 문자열로 전달 받는다.
// fetch: 기본 BASE_URL과 전달받은 엔드포인트 경로를 합친 전체 URLfh GET HTTP 요청을 전송한다.
//      fetch는 404를 에러로 취급하지 않기 때문에, 요청 실패 분기를 명시적으로 사용해야 한다.

// export async function api<T>(path: string): Promise<T> {
//     const res = await fetch(`${BASE_URL}${path}`)

//     if (!res.ok) throw new Error (`요청 실패: ${res.status}`)
//     return res.json()
// }



// 백엔드 구현 이후 (JWT Token)

// 상태 코드를 들고 다니는 커스텀 에러 클래스 구현. 
// 일반 Error는 호출하는 컴포넌트 쪽에서 인증이 풀렸는지(401), 권한이 없는지(403), 단순히 없는 데이터인지(404)
// 구분하기 어려우므로, 상태 코드를 애초에 들고 다니도록 한다.
export class ApiError extends Error {
    status: number

    constructor(status: number) {
        super(`요청 실패: ${status}`)
        this.status = status;
    }
}

// 요청 옵션 타입 지정
//      method: POST, PUT, DELETE 등을 지정. 생략 가능
//      body: 객체나 배열 등 전송할 데이터를 타입에 구애받지 않고 전달받는다.
type Options = {
    method?: string
    body?: unknown
}

// 매개변수 options: Options = {}: 매개변수에 기본값 빈 객체를 주어 옵션 없이 api를 호출해도 에러가 나지 않는다.
// method: 별도로 메서드를 넘기지 않으면 GET 요청으로 동작한다.
// headers:
//      options.body: 서버에 JSON을 보낸다고 알리는 타입들을 스프레드 연산자로 추가한다.
//      token: 토큰이 로컬스토리지에 존재하면 Authorization 란에 Bearer 토큰 헤더를 자동으로 추가한다.
// body: 자바스크립트 객체 options.body를 문자열 형태 JSON.stringify()로 자동 변환해 전송한다.
export async function api<T>(path: string, options: Options = {}): Promise<T> {
    const token = getToken()

    const res = await fetch(`${BASE_URL}${path}`, {
        method: options.method ?? 'GET',
        headers: {
            ...(options.body ? {'Content-Type': 'application/json' } : {}),
            ...(token ? { Authorization: `Bearer ${token}` } : {})
        },
        body: options.body ? JSON.stringify(options.body) : undefined
    })

    if (!res.ok) throw new ApiError(res.status)
    return res.json()
}
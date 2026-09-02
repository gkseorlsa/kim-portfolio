# Project 0: mini project (my-portfolio)

## 학습 내용

1. 오전: Frontend 구현

- 기술 스택: React, Supabase, Vercel

2. 오후: Backend 구현

- 기술 스택: Spring Boot


## 오전 실습: 핵심 주석

entities/api/getPosts.ts
```TypeScript
// 게시글 조회 함수 (GET Posts)
async function getPosts(): Promise<Post[]> {
    // supabase는 data, error, count, status, statusText 등을 반환한다.
    // 이 중, data와 error만 구조 분해 할당 한다.
    //      data: 조회된 데이터 배열 (실패 시 null)
    //      error: 에러 객체 (성공 시 null)
    const { data, error } = await supabase
        .from('posts')
        .select('id, slug, title, summary, created_at')
        .order('created_at', {ascending: false})
    
    if (error) throw error
    return data
}

export default getPosts;
```


pages/BlogPost.tsx
```TypeScript
// 포스트 본문 렌더링

// Blog.tsx 에서는 목록을 불러올 때 React-Query를 사용했으나, 여기선 useEffect + fetch를 사용한다.
//      DB 데이터는 메뉴가 언제 추가되거나 삭제될지 모르는, 수시로 변하는 데이터이다.
//      때문에 React Query가 계속 DB를 확인하며 최신 상태인지 확인하고, 그 결과를 가져와야 한다.

//      반면, 마크다운 파일은 한번 배포해 놓으면 내용이 바뀌지 않는다.
//      이는 크롬 같은 인터넷 브라우저가 알아서 컴퓨터에 저장, 즉 브라우저 캐시 기능을 사용해서 저장해 놓는데,
//      때문에 기본 fetch 기능만 사용하더라도 충분히 빠르게 된다.
function BlogPost() {
    const { slug } = useParams()
    const [ content, setContent ] = useState('')

    useEffect(() => {
        fetch(`/posts/${slug}.md`)
        .then(res => res.text())
        .then(setContent)
    }, [slug])

    return <ReactMarkdown>{content}</ReactMarkdown>
    // ReactMarkdown: 읽어온 텍스트 문자열을 HTML로 파싱하여 렌더링
}

export default BlogPost;
```

entities/session/ui/AuthListener.tsx
```TypeScript
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
```


entities/session/model/authSlice.ts
```TypeScript
// authSlice.ts의 존재 이유:
//      로그인 세션, JWT 토큰, 자동 갱신(Refresh)와 같은 무겁고 민감한 작업은 Supabase SDK가 알아서 처리한다.
//      반면, UI 화면 곳곳(헤더, 마이페이지, 글 작성 게시자 등)에 즉각 렌더링 하기 위한 가벼운 정보(id, email) 등은
//      전역 상태인 Redux에 따로 보관하기 위해 만들어졌다.

// 그러나 슬라이스 하나만으로는 실제로 동작하지 않는다. 슬라이스는 상태를 변경하는 규칙 명세일 뿐이다.
// 때문에 실제로 데이터가 살아 숨쉬는 메모리 창고, store를 추가적으로 구현해야 한다. (store.ts)

// 중앙 저장소 store.ts:
//      슬라이스가 authSlice 하나뿐이어도 Redux가 동작하려면 중앙 Store를 생성해야 한다.
//      앞으로 새로운 슬라이스(리듀서)가 계속 추가될 때, 이 store.ts의 reducer 모음 객체에 쌓기만 하면 된다.
//      추가적으로 RootState와 AppDispatch 타입을 store로부터 자동으로 뽑아낸다면 타입을 자동화할 수 있다.

// 타입 일반화 hooks.ts:
//      컴포넌트마다 매번 긴 타입을 직접 입력하는 보일러플레이트와 휴먼 에러를 방지하기 위해,
//      타입이 고정된 useAppDispatch와 useAppSelector 훅을 사전에 정의해 둔다.

// 앱 전체에 Redux store 정의 <Provider>:
//      어디에서든 Store에 접근할 수 있도록 한다.

// 실행 주체 <AuthListener>:
//      누가, 그리고 언제 setUser 액션을 실제로 dispatch해서 store를 갱신할 것인가 에 대한 실행 주체가 필요하다.



// 인증 토큰과 같은 민감한 정보는 제외한, 화면 UI 렌더링에 필요한 최소한의 식별 정보(id, email)만 정의한다.
type User = {
    id: string
    email: string
}

// 기본적인 Auth 상태 구성 요소. user: 로그인/비로그인, status: 인증 확인 중/확인 완료
type AuthState = {
    user: User | null
    status: 'loading' | 'ready'
}

// 초기화 (AuthState 기반)
const initialState: AuthState = {
    user: null,
    status: 'loading'
}

// 슬라이스 생성. auth/setUser 액션 타입이 생성된다.
// setUser:
//      로그인에 성공하여 유저 정보를 넘겨받거나, 로그아웃하여 null이 전달되었을 때 호출된다.
//      state.user를 전달받은 값 action.payload로 업데이트 한다.
//      state.status를 'ready'로 변경하여 인증 확인 완료를 나타낸다.

// 리듀서가 하나인 이유는 상태를 바꾸는 출처가 Supabase 뿐이기 때문이다.
// 즉, 리듀서 개수는 상태 변화의 종류가 아닌, 출처의 개수를 따라간다.
const authSlice = createSlice({
    name: 'auth',
    initialState,
    reducers: {
        setUser: (
            state,
            action: PayloadAction<User | null>
        ) => {
            state.user = action.payload
            state.status = 'ready'
        }
    }
})

export const { setUser } = authSlice.actions
export default authSlice.reducer
```
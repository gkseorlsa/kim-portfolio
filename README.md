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

## 오후 실습: 핵심 주석

shared/api/client.ts

```JavaScript
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
```

jwt/JwtAuthenticationFilter.java

```Java
// 클라이언트가 보낸 HTTP 요청 헤더에서 JWT를 꺼내 검증하고, 유효한 사용자인 경우 인증 상태로 등록해준다.
//      서블릿(Survlet): 클라이언트의 HTTP 요청을 받아 처리하고, 그 결과를 다시 돌려주는 자바 기반 웹 컴포넌트

// extends OncePerRequestFilter: Spring Security 또는 서블릿 환경에서 단일 HTTP 요청 당
//      딱 한 번만 실행되도록 보장하는 추상 필터 클래스이다. 즉, 중복 실행을 방지한다.
// header 문자열: 클라이언트 요청의 Authorization 헤더 값을 확인한다.
//      내부에서 header가 비어있지 않고, "Bearer "로 시작하는지 검사한다.
// email 문자열: jwtTokenProvider를 통해 토큰의 Signature와 만료시간을 검증한다.
//      Payload에 담긴 사용자 식별 정보(email)을 추출한다.
// SecurityContextHolder.getContext().setAuthentication():
//      토큰이 유효하다면 UsernamePasswordAuthenticationToken 객체를 생성해, SecurityContext에 저장한다.
//      인자:
//          email: 인증된 사용자 식별자
//          null: 자격 증명. 패스워드는 이미 토큰으로 증명되었으므로 불필요하다.
//          List.of(): 부여할 권한 목록. 현재는 Role 없이 빈 리스트를 전달한다.
//      이 작업이 완수되어야 이후 컨트롤러나 시큐리티 인가 설정에서 "로그인된 사용자"로 인정받는다.
// catch: 토큰이 위조되었거나 유효기간이 지났을 경우 발생하는 예외를 잡는다.
//      에러 응답을 따로 만들지 않고, 인증 객체를 등록하지 않은 채 그냥 통과시킨다.
//      이후에 인증이 필요하라면 401/403 등 에러를, 공개 경로면 통과하도록 자연스럽게 처리한다.
// chain.doFilter(): 필터 검증 작업이 끝났으므로 다음 필터로 요청을 전달한다.

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                String email = jwtTokenProvider.getEmail(header.substring(7));
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(email, null, List.of())
                );
            } catch (JwtException e) {
                // 유효하지 않은 토큰: 인증 없이 통과시킨다.
            }
        }

        chain.doFilter(request, response);
    }
}
```

config/SecurityConfig.java
```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // Spring Security를 사용해 REST API에 최적화된 Stateless 보안 규칙과 URL 접근 권한 설정

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // .csrf(csrf -> csrf.disable()):
                //      보통 브라우저 기반은 쿠키 세션 사용 시 CSRF 토큰이 필수적이다.
                //      그러나 REST API는 세션을 저장하지 않는 stateless 방식이므로
                //      토큰(주로 JWT) 기반 인증을 사용할 때는 이를 비활성화 한다.
                // .cors(Customizer.withDefaults()):
                //      WebConfig.java에 지정한 CORS 규칙을 Spring Security 필터 체인에도 적용한다.
                //      이 구문이 있어야 OPTIONS와 같은 Preflight 등이 거부되지 않는다.
                // .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)):
                //      세션 미사용(stateless)를 명시하는 구문
                // .authorizeHttpRequests():
                //      URL 경로 별 인가(Authorization) 정책을 지정한다.
                //          /error:
                //              기본적으로 예외가 발생하면 Spring은 내부적으로 /error로 넘긴다.
                //              이것이 없으면 모든 에러가 403이 되므로 원인 추적이 어렵게 된다.
                //      permitAll(): 인증 없이 누구나 접근 가능하도록 열어둔다.
                //      anyRequest().authenticatied(): 명시된 경로를 제오이한 나머지 요청은 유효한 인증이 필요

                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(s ->
                        s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // 웬만한 경로는 열려 있음
                        .requestMatchers(
                                "/error",
                                "/api/ping",
                                "/api/auth/login",
                                "/api/auth/signup")
                        .permitAll()

                        // GET /api/posts는 열림, POST /api/posts는 닫힘
                        .requestMatchers(HttpMethod.GET, "/api/posts/**")
                        .permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(e -> e.authenticationEntryPoint(
                        (req, res, ex) ->
                                res.sendError(HttpServletResponse.SC_UNAUTHORIZED)
                ))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 비밀번호 암호화 빈
    //      회원가입 시 사용자의 비밀번호를 BCrypt으로 암호화하도록 설정 (해시값)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

config/WebConfig.java

```Java
// 보통의 브라우저는 보안 정책 때문에 주소의 도메인(IP), 포트, 프로토콜 중 하나라도 다르면 다른 출처(origin)으로
//      간주하고, 요청 결과를 기본적으로 차단한다.
// 그러나 CORS 설정을 사용한다면 프론트엔드(localhost:5173)에서 오는 요청은 안전하니 허용해 달라고
//      브라우저에 명시해 줄 수 있다.

// WebMvcConfigurer: Spring MVC의 기본 웹 설정을 가로채 커스텀 설정을 추가할 수 있게 해주는 인터페이스
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // CORS 관련 규칙을 정의하는 전용 메서드 오버라이드
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 서버의 엔드포인트 중 /api/로 시작하는 모든 URL 경로에 대해 아래 규칙을 적용
        //      allowedOrigins: 허용 출처 지정
        //      allowedMethods: 허용할 HTTP 메서드 명시
        //          OPTIONS: 사전 요청(Preflight) 메서드. 전송 전 미리 허용 하는지 확인한다.
        //      allowedHeaders: 클라이언트가 요청 시 전송할 헤더 (Content-Type, Authorization 등) 허용
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}

```

post/PostService.java

```Java
@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

    // 모든 포스트 조회
    public List<PostResponse> getPosts() {
        return postRepository.findAll(
                // Sort.Direction.DESC: 정렬 방향 지정. 내림차순.
                //      가장 최근에 작성된 글이 맨 위에 오도록 의도한다.

                // createdAt: 정렬할 기준 대상의 이름.
                //      실제 DB 컬럼명 created_at이 아닌, 자바 클래스 필드 변수명 createdAt을 사용해야
                //      JPA가 이를 인식할 수 있다. created_at 컬럼으로 자동 변환한다.
                Sort.by(Sort.Direction.DESC, "createdAt")
        )
                .stream()
                .map(PostResponse::from)
                .toList();
    }

    // slug 로 포스트 조회
    public PostDetailResponse getBySlug(String slug) {
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return PostDetailResponse.from(post);
    }

    // 포스트 작성
    public PostDetailResponse post(Post post) {
        Post saved = postRepository.save(post);
        return PostDetailResponse.from(saved);
    }
}

```

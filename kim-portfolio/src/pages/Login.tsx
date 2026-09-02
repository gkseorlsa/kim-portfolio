import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { supabase } from "../shared/api/supabase";

// 로그인 페이지

// 해당 페이지엔 dispatch 코드가 없다.
//      로그인 성공 직후 dispatch(setUser(data.user)) 를 호출할 만도 한데, 해당 아키텍처에선 제외되어 있다.
//      Login.tsx는 supabase의 signInWithPassword로 로그인 요청만 보낸다.
//      AuthListener가 이를 실시간으로 감지하고 있어서, 알아서 dispatch를 실행하고 store를 채워준다.
//      따라서 단순히 페이지 이동(navigate('/'))만 시켜주면 된다.

// 만약 수동으로 dispatch를 호출한다고 가정해보자. 그러면 다양한 로그인/인증 시나리오마다 dispatch 코드를
// 중복해서 작성해야 하는 문제가 생긴다.
//      로그인 페이지에서 로그인 할 때 dispatch
//      새로고침 했을 때 dispatch
//      다른 브라우저 탭에서 로그인 했을 때 dispatch
//      토큰이 만료되어 자동 갱신 되었을 때 dispatch...
function Login() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState<string | null>(null);
    const navigate = useNavigate();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);

    const { error } = await supabase.auth.signInWithPassword({
            email,
            password
        });

    if (error) {
            // 에러 메시지 보안: 무엇이 틀렸는지 알려주지 않음
            setError('이메일 또는 비밀번호를 확인해주세요.');
            return;
    }

        navigate('/');
    };

    return (
        <div>
            <h1>Login</h1>
            {error && <p style={{ color: 'red' }}>{error}</p>}
            <form onSubmit={handleSubmit}>
                <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="Email"
                    required
                />
                <input
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="Password"
                    required
                />
                <button type="submit">Login</button>
            </form>
        </div>
    );
}

export default Login;

import { useQuery } from "@tanstack/react-query";
import getPosts from "../entities/api/getPosts";
import { Link } from "react-router-dom";

function Blog() {

    // getPosts 함수 호출. useQuery는 DB에 직접 접근하여 데이터를 가져오는 GET 방식이다. 상태 3개를 받는다.
    const { data: posts, isLoading, error } = useQuery({
        queryKey: ['posts'],    // 캐싱 식별자
        queryFn: getPosts       // async 함수
    })

    if (isLoading) return <p>불러오는 중...</p>
    if (error) return <p>에러: {String(error)}</p>

    return (
        <div>
            <ul>
                {posts?.map(post => (
                    <li key={post.id}>
                        <Link to={`/blog/${post.slug}`}>{post.title}</Link>
                        <p>{post.summary}</p>
                    </li>
                ))}
            </ul>
        </div>
    )
}

export default Blog;
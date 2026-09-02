import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import ReactMarkdown from 'react-markdown';
import { api } from "../shared/api/client";
import type { PostDetail } from "../entities/post/model/types";

// 포스트 본문 렌더링

function BlogPost() {

    // 백엔드 구현 이전 (supabase 사용)

    // Blog.tsx 에서는 목록을 불러올 때 React-Query를 사용했으나, 여기선 useEffect + fetch를 사용한다.
    //      DB 데이터는 메뉴가 언제 추가되거나 삭제될지 모르는, 수시로 변하는 데이터이다.
    //      때문에 React Query가 계속 DB를 확인하며 최신 상태인지 확인하고, 그 결과를 가져와야 한다.

    //      반면, 마크다운 파일은 한번 배포해 놓으면 내용이 바뀌지 않는다.
    //      이는 크롬 같은 인터넷 브라우저가 알아서 컴퓨터에 저장, 즉 브라우저 캐시 기능을 사용해서 저장해 놓는데,
    //      때문에 기본 fetch 기능만 사용하더라도 충분히 빠르게 된다.

    // const { slug } = useParams()
    // const [ content, setContent ] = useState('')

    // useEffect(() => {
    //     fetch(`/posts/${slug}.md`)
    //     .then(res => res.text())
    //     .then(setContent)
    // }, [slug])



    // 백엔드 구현 이후 코드 (api 연동)

    // 기본적인 틀은 백엔드 구현 이전과 비슷하나, fetch 대신 미리 구현한 api 메서드를 사용한다.
    // 백엔드에 넣어진 포스트 본문을 참조할 것이므로, slug.md를 보는 것이 아닌, slug만 참조한다.

    const { slug } = useParams()
    const [content, setContent] = useState('')

    useEffect(() => {
        api<PostDetail>(`/api/posts/${slug}`)
        .then(post => setContent(post.content))
        .catch(() => setContent('글을 찾을 수 없습니다.'))
    }, [slug])

    



    // // ReactMarkdown: 읽어온 텍스트 문자열을 HTML로 파싱하여 렌더링
    return <ReactMarkdown>{content}</ReactMarkdown>
}

export default BlogPost;
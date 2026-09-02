import { api } from "../../shared/api/client";
import { supabase } from "../../shared/api/supabase";
import type { Post } from "../post/model/types";

// 게시글 조회 함수 (GET Posts)
async function getPosts(): Promise<Post[]> {
    // 백엔드 구현 이전 코드 (supabase 이용)

    // supabase는 data, error, count, status, statusText 등을 반환한다.
    // 이 중, data와 error만 구조 분해 할당 한다.
    //      data: 조회된 데이터 배열 (실패 시 null)
    //      error: 에러 객체 (성공 시 null)

    //     const { data, error } = await supabase
    //         .from('posts')
    //         .select('id, slug, title, summary, created_at')
    //         .order('created_at', {ascending: false})
    
    //     if (error) throw error
    //     return data

    // 백엔드 구현 이후 코드 (api 연동)

    // 백엔드 구현 이전 코드는 프론트가 Supabase 클라이언트를 통해 SELECT, FROM과 같은 쿼리를 직접 날렸다.
    // 그러나 이제 쿼리 및 데이터 정렬 로직은 백엔드 서버 Controller에 위임하도록 변경했다.
    // 프론트는 완성된 데이터만 REST API로 요청 할 뿐이다.
    // api 메서드는 shared/api/client.ts 에서 가져왔다.
    // 브라우저는 누가 데이터를 주는지 모르므로, 주는 쪽만 바꾸면 된다.
    return api<Post[]>('/api/posts')
}

export default getPosts;
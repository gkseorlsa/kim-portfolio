// Post domain

// content 필드 제외한 타입. 본문은 .md 파일로 보관한 형태
export interface Post {
    id: number
    slug: string
    title: string
    summary: string
    created_at: string  // JSON으로 오기 때문에 DATE가 아닌 string 사용
}

// content 필드까지 포함한 타입
export interface PostDetail extends Post {
    content: string
}
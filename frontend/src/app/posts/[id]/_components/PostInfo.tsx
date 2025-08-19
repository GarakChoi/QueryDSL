import usePost from "@/domain/post/hooks/usePost";

import Link from "next/link";
import { useRouter } from "next/navigation";

export default function PostInfo({
  postState,
}: {
  postState: ReturnType<typeof usePost>;
}) {
  const router = useRouter();
  const { post, deletePost: _deletePost } = postState;

  if (post == null) return <div>로딩중...</div>;

  const deletePost = () => {
    if (!confirm(`${post.id}번 글을 정말로 삭제하시겠습니까?`)) return;

    _deletePost(post.id, () => {
      router.replace("/posts");
    });
  };

  return (
    <>
      <div>번호 : {post.id}</div>
      <div>제목: {post.title}</div>
      <div style={{ whiteSpace: "pre-line" }}>{post.content}</div>

      <div className="flex gap-2">
        <button className="p-2 rounded border" onClick={deletePost}>
          삭제
        </button>
        <Link className="p-2 rounded border" href={`/posts/${post.id}/edit`}>
          수정
        </Link>
      </div>
    </>
  );
}

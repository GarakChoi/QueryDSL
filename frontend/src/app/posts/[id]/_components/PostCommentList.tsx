import usePostComments from "../_hooks/usePostComments";
import PostCommentListItem from "./PostCommentListItem";

export default function PostCommentList({
  postCommentsState,
}: {
  postCommentsState: ReturnType<typeof usePostComments>;
}) {
  const { postId, postComments } = postCommentsState;

  if (postComments == null) return <div>로딩중...</div>;

  return (
    <>
      <h2>{postId}번 글에 대한 댓글 목록</h2>

      {postComments != null && postComments.length == 0 && (
        <div>댓글이 없습니다.</div>
      )}

      {postComments != null && postComments.length > 0 && (
        <ul className="mt-2 flex flex-col gap-2">
          {postComments.map((comment) => (
            <PostCommentListItem
              key={comment.id}
              comment={comment}
              postCommentsState={postCommentsState}
            />
          ))}
        </ul>
      )}
    </>
  );
}

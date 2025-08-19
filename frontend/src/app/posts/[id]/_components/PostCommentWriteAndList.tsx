import usePostComments from "../_hooks/usePostComments";
import PostCommentList from "./PostCommentList";
import PostCommentWrite from "./PostCommentWrite";

export default function PostCommentWriteAndList({
  postCommentsState,
}: {
  postCommentsState: ReturnType<typeof usePostComments>;
}) {
  return (
    <>
      <PostCommentWrite postCommentsState={postCommentsState} />

      <PostCommentList postCommentsState={postCommentsState} />
    </>
  );
}

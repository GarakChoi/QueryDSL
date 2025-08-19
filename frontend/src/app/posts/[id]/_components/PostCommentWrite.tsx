import usePostComments from "../_hooks/usePostComments";

export default function PostCommentWrite({
  postCommentsState,
}: {
  postCommentsState: ReturnType<typeof usePostComments>;
}) {
  const { postId, writeComment } = postCommentsState;

  const handleCommentWriteFormSubmit = (
    e: React.FormEvent<HTMLFormElement>,
  ) => {
    e.preventDefault();

    const form = e.target as HTMLFormElement;

    const contentTextarea = form.elements.namedItem(
      "content",
    ) as HTMLTextAreaElement;

    contentTextarea.value = contentTextarea.value.trim();

    if (contentTextarea.value.length === 0) {
      alert("댓글 내용을 입력해주세요.");
      contentTextarea.focus();
      return;
    }

    if (contentTextarea.value.length < 2) {
      alert("댓글 내용을 2자 이상 입력해주세요.");
      contentTextarea.focus();
      return;
    }

    writeComment(contentTextarea.value, (data) => {
      alert(data.msg);
      contentTextarea.value = "";
    });
  };

  return (
    <>
      <h2>{postId}번글에 대한 댓글 작성</h2>

      <form
        className="flex gap-2 items-center"
        onSubmit={handleCommentWriteFormSubmit}
      >
        <textarea
          className="border p-2 rounded"
          name="content"
          placeholder="댓글 내용"
          maxLength={100}
          rows={5}
        />
        <button className="p-2 rounded border" type="submit">
          작성
        </button>
      </form>
    </>
  );
}

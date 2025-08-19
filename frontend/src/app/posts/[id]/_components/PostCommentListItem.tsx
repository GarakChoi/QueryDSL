import type { components } from "@/global/backend/apiV1/schema";
import { useState } from "react";

import usePostComments from "../_hooks/usePostComments";

type PostCommentDto = components["schemas"]["PostCommentDto"];

export default function PostCommentListItem({
  comment,
  postCommentsState,
}: {
  comment: PostCommentDto;
  postCommentsState: ReturnType<typeof usePostComments>;
}) {
  const [modifyMode, setModifyMode] = useState(false);
  const { deleteComment: _deleteComment, modifyComment } = postCommentsState;

  const toggleModifyMode = () => {
    setModifyMode(!modifyMode);
  };

  const deleteComment = (commentId: number) => {
    if (!confirm(`${commentId}번 댓글을 정말로 삭제하시겠습니까?`)) return;

    _deleteComment(commentId, (data) => {
      alert(data.msg);
    });
  };

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
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

    modifyComment(comment.id, contentTextarea.value, (data) => {
      alert(data.msg);
      toggleModifyMode();
    });
  };

  return (
    <li className="flex gap-2 items-start">
      <span>{comment.id} :</span>
      {!modifyMode && (
        <span style={{ whiteSpace: "pre-line" }}>{comment.content}</span>
      )}
      {modifyMode && (
        <form className="flex gap-2 items-start" onSubmit={handleSubmit}>
          <textarea
            className="border p-2 rounded"
            name="content"
            placeholder="댓글 내용"
            maxLength={100}
            rows={5}
            defaultValue={comment.content}
            autoFocus
          />
          <button className="p-2 rounded border" type="submit">
            저장
          </button>
        </form>
      )}
      <button className="p-2 rounded border" onClick={toggleModifyMode}>
        {modifyMode ? "수정취소" : "수정"}
      </button>
      <button
        className="p-2 rounded border"
        onClick={() => deleteComment(comment.id)}
      >
        삭제
      </button>
    </li>
  );
}

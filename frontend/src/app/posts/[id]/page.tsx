"use client";

import usePost from "@/domain/post/hooks/usePost";
import { use } from "react";

import PostCommentWriteAndList from "./_components/PostCommentWriteAndList";
import PostInfo from "./_components/PostInfo";
import usePostComments from "./_hooks/usePostComments";

export default function Page({ params }: { params: Promise<{ id: string }> }) {
  const { id: idStr } = use(params);
  const id = parseInt(idStr);

  const postState = usePost(id);
  const postCommentsState = usePostComments(id);

  return (
    <>
      <h1>글 상세페이지</h1>

      <PostInfo postState={postState} />

      <PostCommentWriteAndList postCommentsState={postCommentsState} />
    </>
  );
}

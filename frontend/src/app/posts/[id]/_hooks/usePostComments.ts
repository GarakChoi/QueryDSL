import type { components } from "@/global/backend/apiV1/schema";
import client from "@/global/backend/client";
import { useEffect, useState } from "react";

type PostCommentDto = components["schemas"]["PostCommentDto"];
type RsDataVoid = components["schemas"]["RsDataVoid"];
type RsDataPostCommentDto = components["schemas"]["RsDataPostCommentDto"];

export default function usePostComments(postId: number) {
  const [postComments, setPostComments] = useState<PostCommentDto[] | null>(
    null,
  );

  useEffect(() => {
    client
      .GET("/api/v1/posts/{postId}/comments", {
        params: {
          path: {
            postId,
          },
        },
      })
      .then((res) => {
        if (res.error) {
          alert(res.error.msg);
          return;
        }
        setPostComments(res.data);
      });
  }, [postId]);

  const deleteComment = (
    commentId: number,
    onSuccess: (data: RsDataVoid) => void,
  ) => {
    client
      .DELETE("/api/v1/posts/{postId}/comments/{id}", {
        params: {
          path: {
            postId,
            id: commentId,
          },
        },
      })
      .then((res) => {
        if (res.error) {
          alert(res.error.msg);
          return;
        }

        if (postComments == null) return;

        setPostComments(postComments.filter((c) => c.id != commentId));

        onSuccess(res.data);
      });
  };

  const writeComment = (
    content: string,
    onSuccess: (data: RsDataPostCommentDto) => void,
  ) => {
    client
      .POST("/api/v1/posts/{postId}/comments", {
        params: {
          path: {
            postId,
          },
        },
        body: {
          content,
        },
      })
      .then((res) => {
        if (res.error) {
          alert(res.error.msg);
          return;
        }

        if (postComments == null) return;

        setPostComments([...postComments, res.data.data]);

        onSuccess(res.data);
      });
  };

  const modifyComment = (
    commentId: number,
    content: string,
    onSuccess: (data: RsDataVoid) => void,
  ) => {
    client
      .PUT("/api/v1/posts/{postId}/comments/{id}", {
        params: {
          path: {
            postId,
            id: commentId,
          },
        },
        body: {
          content,
        },
      })
      .then((res) => {
        if (res.error) {
          alert(res.error.msg);
          return;
        }

        if (postComments == null) return;

        setPostComments(
          postComments.map((comment) =>
            comment.id === commentId ? { ...comment, content } : comment,
          ),
        );

        onSuccess(res.data);
      });
  };

  return {
    postId,
    postComments,
    deleteComment,
    writeComment,
    modifyComment,
  };
}

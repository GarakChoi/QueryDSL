import type { components } from "@/global/backend/apiV1/schema";
import client from "@/global/backend/client";
import { useEffect, useState } from "react";

type PostWithContentDto = components["schemas"]["PostWithContentDto"];
type RsDataVoid = components["schemas"]["RsDataVoid"];

export default function usePost(id: number) {
  const [post, setPost] = useState<PostWithContentDto | null>(null);

  useEffect(() => {
    client
      .GET("/api/v1/posts/{id}", {
        params: {
          path: {
            id,
          },
        },
      })
      .then((res) => {
        if (res.error) {
          alert(res.error.msg);
          return;
        }

        setPost(res.data);
      });
  }, [id]);

  const deletePost = (id: number, onSuccess: () => void) => {
    client
      .DELETE("/api/v1/posts/{id}", {
        params: {
          path: {
            id,
          },
        },
      })
      .then((res) => {
        if (res.error) {
          alert(res.error.msg);
          return;
        }

        onSuccess();
      });
  };

  const modifyPost = (
    id: number,
    title: string,
    content: string,
    onSuccess: (res: RsDataVoid) => void,
  ) => {
    client
      .PUT("/api/v1/posts/{id}", {
        params: {
          path: {
            id,
          },
        },
        body: {
          title,
          content,
        },
      })
      .then((res) => {
        if (res.error) {
          alert(res.error.msg);
          return;
        }

        onSuccess(res.data);
      });
  };

  return {
    post,
    deletePost,
    modifyPost,
  };
}

"use client";

import type { components } from "@/global/backend/apiV1/schema";
import client from "@/global/backend/client";
import { useEffect, useState } from "react";

import Link from "next/link";

type PostDto = components["schemas"]["PostDto"];

export default function Page() {
  const [posts, setPosts] = useState<PostDto[] | null>(null);

  useEffect(() => {
    client.GET("/api/v1/posts").then((res) => res.data && setPosts(res.data));
  }, []);

  if (posts == null) return <div>로딩중...</div>;

  return (
    <>
      <h1>글 목록</h1>

      {posts.length == 0 && <div>글이 없습니다.</div>}

      {posts.length > 0 && (
        <ul>
          {posts.map((post) => (
            <li key={post.id}>
              <Link href={`/posts/${post.id}`}>
                {post.id} : {post.title}
              </Link>
            </li>
          ))}
        </ul>
      )}

      <div>
        <Link href="/posts/write">글쓰기</Link>
      </div>
    </>
  );
}

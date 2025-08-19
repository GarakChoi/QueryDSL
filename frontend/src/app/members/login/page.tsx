"use client";

import withLogout from "@/global/auth/hoc/withLogout";
import { useAuthContext } from "@/global/auth/hooks/useAuth";
import client from "@/global/backend/client";

import { useRouter } from "next/navigation";

export default withLogout(function Page() {
  const router = useRouter();
  const { setLoginMember } = useAuthContext();

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    const form = e.target as HTMLFormElement;

    const usernameInput = form.elements.namedItem(
      "username",
    ) as HTMLInputElement;
    const passwordInput = form.elements.namedItem(
      "password",
    ) as HTMLTextAreaElement;

    usernameInput.value = usernameInput.value.trim();

    if (usernameInput.value.length === 0) {
      alert("아이디를 입력해주세요.");
      usernameInput.focus();
      return;
    }

    if (usernameInput.value.length < 2) {
      alert("아이디를 2자 이상 입력해주세요.");
      usernameInput.focus();
      return;
    }

    passwordInput.value = passwordInput.value.trim();

    if (passwordInput.value.length === 0) {
      alert("비밀번호를 입력해주세요.");
      passwordInput.focus();
      return;
    }

    if (passwordInput.value.length < 2) {
      alert("비밀번호를 2자 이상 입력해주세요.");
      passwordInput.focus();
      return;
    }

    client
      .POST("/api/v1/members/login", {
        body: {
          username: usernameInput.value,
          password: passwordInput.value,
        },
      })
      .then((res) => {
        if (res.error) {
          alert(res.error.msg);
          return;
        }

        setLoginMember(res.data.data.item);

        alert(res.data.msg);
        router.replace(`/`);
      });
  };

  return (
    <>
      <h1>로그인</h1>

      <form className="flex flex-col gap-2 p-2" onSubmit={handleSubmit}>
        <input
          className="border p-2 rounded"
          type="text"
          name="username"
          placeholder="아이디"
          autoFocus
          maxLength={30}
        />
        <input
          className="border p-2 rounded"
          type="password"
          name="password"
          placeholder="비밀번호"
          maxLength={30}
        />
        <button className="border p-2 rounded" type="submit">
          로그인
        </button>
      </form>
    </>
  );
});

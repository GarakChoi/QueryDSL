"use client";

import { AuthContext, useAuthContext } from "@/global/auth/hooks/useAuth";

import Link from "next/link";
import { useRouter } from "next/navigation";

export default function ClientLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const authState = useAuthContext();
  const router = useRouter();

  const { loginMember, isLogin, logout: _logout } = authState;

  const apiBaseUrl = process.env.NEXT_PUBLIC_API_BASE_URL as string;
  const frontendBaseUrl = process.env.NEXT_PUBLIC_FRONTEND_BASE_URL as string;
  const redirectUrl = encodeURIComponent(`${frontendBaseUrl}/members/me`);

  const loginUrl = (providerTypeCode: string) =>
    `${apiBaseUrl}/oauth2/authorization/${providerTypeCode}?redirectUrl=${redirectUrl}`;

  const logout = () => {
    _logout(() => router.replace("/"));
  };

  return (
    <AuthContext value={authState}>
      <header>
        <nav className="flex">
          <Link href="/" className="p-2 rounded hover:bg-gray-100">
            메인
          </Link>
          <Link href="/posts" className="p-2 rounded hover:bg-gray-100">
            글 목록
          </Link>
          {!isLogin && (
            <>
              <Link
                href="/members/login"
                className="p-2 rounded hover:bg-gray-100"
              >
                로그인
              </Link>
              <a
                href={loginUrl("kakao")}
                className="p-2 rounded hover:bg-gray-100"
              >
                카카오 로그인
              </a>
              <a
                href={loginUrl("google")}
                className="p-2 rounded hover:bg-gray-100"
              >
                구글 로그인
              </a>
              <a
                href={loginUrl("naver")}
                className="p-2 rounded hover:bg-gray-100"
              >
                네이버 로그인
              </a>
            </>
          )}
          {isLogin && (
            <button
              onClick={logout}
              className="p-2 rounded hover:bg-gray-100 flex"
            >
              로그아웃
            </button>
          )}
          {isLogin && (
            <Link
              href="/members/me"
              className="p-2 rounded hover:bg-gray-100 flex gap-2"
            >
              <span>{loginMember.name}님의 정보</span>
              <img
                src={loginMember.profileImageUrl}
                width="30"
                alt=""
                className="rounded-full object-cover aspect-[1/1]"
              />
            </Link>
          )}
        </nav>
      </header>
      <main className="flex-1 flex flex-col">{children}</main>
      <footer className="text-center p-2">푸터</footer>
    </AuthContext>
  );
}

"use client";

import { useAuthContext } from "@/global/auth/hooks/useAuth";

export default function withLogin<P extends object>(
  Component: React.ComponentType<P>,
) {
  return function WithLoginComponent(props: P) {
    const { isLogin } = useAuthContext();

    if (!isLogin) {
      return <div>로그인 후 이용해주세요.</div>;
    }

    return <Component {...props} />;
  };
}

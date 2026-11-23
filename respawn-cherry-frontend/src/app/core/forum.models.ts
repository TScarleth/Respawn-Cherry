export type UserRole = 'USUARIO' | 'ADMIN';

export interface ForumUser {
  id: string;
  name: string;
  email: string;
  role: UserRole;
  joinedAt: string;
  bio?: string;
  phone?: string;
  photo?: string;
  country?: string;
  showCountry?: boolean;
}

export interface ForumCategory {
  id: string;
  slug: string;
  name: string;
  description: string;
  color: string;
  icon: string;
}

export interface ForumPost {
  id: string;
  authorId: string;
  authorName: string;
  categoryId: string;
  title: string;
  body: string;
  createdAt: string;
  likes: number;
  liked: boolean;
  imageIds: string[];
}

export interface ForumComment {
  id: string;
  postId: string;
  authorId: string;
  authorName: string;
  body: string;
  createdAt: string;
}

export interface PersistedForumState {
  posts: ForumPost[];
  comments: ForumComment[];
  users?: ForumUser[];
  passwords?: Record<string, string>;
  sessionEmail?: string | null;
}

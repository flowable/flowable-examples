export type FitBounds = [[number, number], [number, number]];

export type Comment = {
  id: string;
  user: string;
  station?: number;
  rating: number;
  comment?: string;
  avatarUrl?: string;
};

export type MarkerProps = {
  id: number;
  coordinates: number[];
  comment: Comment;
};

export type FormData = {
  user: string;
  rating: number;
  comment: string;
};

export type SettingsData = {
  speed: string;
}

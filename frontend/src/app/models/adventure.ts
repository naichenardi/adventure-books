export type Difficulty = 'EASY' | 'MEDIUM' | 'HARD';
export type SectionType = 'BEGIN' | 'NODE' | 'END';
export type ConsequenceType = 'LOSE_HEALTH' | 'GAIN_HEALTH';

export interface Consequence {
  type: ConsequenceType;
  value?: string;
  text?: string;
}

export interface StoryOption {
  description: string;
  gotoId: string;
  consequence?: Consequence;
}

export interface Section {
  id: string;
  text: string;
  type: SectionType;
  options?: StoryOption[];
}

export interface Book {
  id?: number;
  title: string;
  author?: string;
  difficulty?: Difficulty;
  sections?: Section[];
}

export interface GameSession {
  id?: number;
  bookId?: number;
  currentSectionId?: string;
  health?: number;
  finished?: boolean;
  saved?: boolean;
  active?: boolean;
  history?: string[];
}

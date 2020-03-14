import React, { useState } from 'react';
import classNames from 'classnames';

import './avatar.scss';

export type AvatarProps = {
  id: string;
  avatarUrl?: string;
  displayName?: string;
};

export type AvatarState = {
  imageStatus: 'loading' | 'calm' | 'hide';
};

export const Avatar = ({ id, avatarUrl, displayName }: AvatarProps) => {
  const [imageStatus, setImageStatus] = useState((avatarUrl && 'calm') || 'hide');
  const renderImage = avatarUrl ? imageStatus === 'calm' : false;
  return (
    <div
      key={`${id}-avatar`}
      className={classNames('flw-avatar', {
        'flw-avatar--image': renderImage
      })}>
      {avatarUrl && (
        <img
          key={`${id}-img`}
          src={avatarUrl}
          alt={displayName}
          title={displayName}
          onLoad={() => setImageStatus('calm')}
          onError={() => setImageStatus('hide')}
        />
      )}
      {!renderImage && initialsFromName(displayName)}
    </div>
  );
};

const DEFAULT_INITIALS = 'Anonymous User';

export function initialsFromName(displayName?: string): string {
  if (displayName) {
    return (displayName || DEFAULT_INITIALS)
      .split(' ')
      .map(e => e[0])
      .slice(0, 2)
      .join('')
      .toUpperCase();
  }
  return DEFAULT_INITIALS;
}

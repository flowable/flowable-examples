import React from 'react';

import { Comment } from './model';
import { Avatar } from './Avatar';
import { Rating } from './Rating';
import './commentMarker.scss';

export const CommentMarker = (props: Comment) => (
  <div key={props.id} className="flw-commentMarker">
    <div className="flw-commentMarker__content">
      <div className="flw-commentMarker__avatar">
        <Avatar id={`${props.id}_avatar`} displayName={props.user} avatarUrl={props.avatarUrl} />
      </div>
      <div>
        <div className="flw-commentMarker__content__user">{props.user}</div>
        <Rating rate={props.rating} />
      </div>
    </div>
    <div className="flw-commentMarker__content__comment">{props.comment}</div>
  </div>
);

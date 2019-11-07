import React, { useState, FormEvent, useCallback } from 'react';
import keycode from 'keycode';
import classNames from 'classnames';

import { FormData } from './model';
import { Rating } from './Rating';

import './stationMapForm.scss';

type stationMapFormProps = {
  onFormSubmit?: any;
  station?: number;
  className?: string;
};
export const StationMapForm = (props: stationMapFormProps) => {
  let [formData, setFormData] = useState<FormData>({
    user: '',
    rating: 0,
    comment: ''
  });

  const onCommentChange = useCallback(
    (e: FormEvent<HTMLTextAreaElement>) => {
      setFormData({ ...formData, comment: e.currentTarget.value });
    },
    [formData]
  );

  const onNameChange = useCallback(
    (e: FormEvent<HTMLInputElement>) => {
      setFormData({ ...formData, user: e.currentTarget.value });
    },
    [formData]
  );

  const onRatingChange = useCallback(
    (i: number) => {
      setFormData({ ...formData, rating: i });
    },
    [formData]
  );

  const onKeyDown = useCallback(
    (e: React.KeyboardEvent<HTMLInputElement | HTMLTextAreaElement>) => {
      if (keycode.isEventKey(e.nativeEvent, 'enter') && isValid(formData)) {
        e.preventDefault();
        onSubmit();
      }
    },
    [formData]
  );

  const onSubmit = () => {
    props.onFormSubmit && props.onFormSubmit({ ...formData, station: props.station });
    setFormData({
      user: '',
      rating: 0,
      comment: ''
    });
  };

  const containerClass = classNames('flw-stationMap-form', {
    [props.className || '']: !!props.className
  });

  return (
    <div className={containerClass}>
      <span className="flw-stationMap-form__title">Add a Comment</span>
      <div>
        <div className="flw-stationMap-form__label">Name</div>
        <div className="flw-stationMap-form__input">
          <input onChange={onNameChange} value={formData.user} onKeyDown={onKeyDown} />
        </div>
      </div>
      <div>
        <div className="flw-stationMap-form__label">Rating</div>
        <Rating rate={formData.rating} onClick={onRatingChange} />
      </div>
      <div>
        <div className="flw-stationMap-form__label">Comment</div>
        <div className="flw-stationMap-form__input">
          <textarea onChange={onCommentChange} value={formData.comment} onKeyDown={onKeyDown} />
        </div>
      </div>
      <button disabled={!isValid(formData)} onClick={onSubmit}>
        submit
      </button>
    </div>
  );
};

const isValid = (value: FormData): boolean => !!(value.user && value.rating && value.comment);

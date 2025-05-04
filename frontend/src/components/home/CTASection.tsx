import React from 'react';
import styled from 'styled-components';
import { useNavigate } from 'react-router-dom';

const CTAContainer = styled.section`
  padding: 5rem 2rem;
  background: linear-gradient(135deg,rgb(219, 116, 52),rgb(152, 56, 30));
  color: white;
  text-align: center;
`;

const Content = styled.div`
  max-width: 600px;
  margin: 0 auto;
`;

const Title = styled.h2`
  font-size: 2.5rem;
  margin-bottom: 1.5rem;
`;

const ButtonGroup = styled.div`
  display: flex;
  gap: 1rem;
  justify-content: center;
  margin-top: 2rem;
`;

interface ButtonProps {
  primary?: boolean;
}

const Button = styled.button<ButtonProps>`
  padding: 1rem 2rem;
  border-radius: 4px;
  border: 2px solid white;
  font-size: 1.1rem;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: ${(props: ButtonProps) => props.primary ? 'white' : 'transparent'};
  color: ${(props: ButtonProps) => props.primary ? '#e67e22' : 'white'};

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  }
`;

const CTASection = () => {
  const navigate = useNavigate();

  return (
    <CTAContainer>
      <Content>
        <Title>Ready to Play?</Title>
        <ButtonGroup>
          <Button primary onClick={() => navigate('/signup')}>Sign Up</Button>
          <Button onClick={() => navigate('/signin')}>Sign In</Button>
        </ButtonGroup>
      </Content>
    </CTAContainer>
  );
};

export default CTASection;
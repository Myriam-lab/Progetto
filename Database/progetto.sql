PGDMP                      }           postgres    17.4    17.4     �           0    0    ENCODING    ENCODING        SET client_encoding = 'UTF8';
                           false            �           0    0 
   STDSTRINGS 
   STDSTRINGS     (   SET standard_conforming_strings = 'on';
                           false            �           0    0 
   SEARCHPATH 
   SEARCHPATH     8   SELECT pg_catalog.set_config('search_path', '', false);
                           false            �           1262    5    postgres    DATABASE     n   CREATE DATABASE postgres WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'it-IT';
    DROP DATABASE postgres;
                     postgres    false            �           0    0    DATABASE postgres    COMMENT     N   COMMENT ON DATABASE postgres IS 'default administrative connection database';
                        postgres    false    4823            �            1259    24585 
   passeggeri    TABLE     �   CREATE TABLE public.passeggeri (
    ssn character varying(16) NOT NULL,
    nome character varying(100) NOT NULL,
    cognome character varying(100) NOT NULL,
    email character varying(100),
    telefono character varying(20)
);
    DROP TABLE public.passeggeri;
       public         heap r       postgres    false            �            1259    24591    prenotazioni    TABLE       CREATE TABLE public.prenotazioni (
    id_prenotazione integer NOT NULL,
    codice_volo_fk character varying(20),
    ssn_passeggero_fk character varying(16),
    posto character varying(5) NOT NULL,
    assicurazione boolean DEFAULT false,
    bagaglio boolean DEFAULT false
);
     DROP TABLE public.prenotazioni;
       public         heap r       postgres    false            �            1259    24590     prenotazioni_id_prenotazione_seq    SEQUENCE     �   CREATE SEQUENCE public.prenotazioni_id_prenotazione_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 7   DROP SEQUENCE public.prenotazioni_id_prenotazione_seq;
       public               postgres    false    221            �           0    0     prenotazioni_id_prenotazione_seq    SEQUENCE OWNED BY     e   ALTER SEQUENCE public.prenotazioni_id_prenotazione_seq OWNED BY public.prenotazioni.id_prenotazione;
          public               postgres    false    220            �            1259    16400    progetto    TABLE     �   CREATE TABLE public.progetto (
    pname character varying(15) NOT NULL,
    pnumber integer NOT NULL,
    plocation character varying(15),
    dnum integer NOT NULL
);
    DROP TABLE public.progetto;
       public         heap r       postgres    false            �            1259    24580    voli    TABLE     �  CREATE TABLE public.voli (
    codice_volo character varying(20) NOT NULL,
    compagnia_aerea character varying(100) NOT NULL,
    tipo_volo character varying(10) NOT NULL,
    origine character varying(100) NOT NULL,
    destinazione character varying(100) NOT NULL,
    data_volo date NOT NULL,
    orario_previsto time without time zone NOT NULL,
    stato character varying(50),
    gate_numero integer
);
    DROP TABLE public.voli;
       public         heap r       postgres    false            -           2604    24594    prenotazioni id_prenotazione    DEFAULT     �   ALTER TABLE ONLY public.prenotazioni ALTER COLUMN id_prenotazione SET DEFAULT nextval('public.prenotazioni_id_prenotazione_seq'::regclass);
 K   ALTER TABLE public.prenotazioni ALTER COLUMN id_prenotazione DROP DEFAULT;
       public               postgres    false    220    221    221            �          0    24585 
   passeggeri 
   TABLE DATA           I   COPY public.passeggeri (ssn, nome, cognome, email, telefono) FROM stdin;
    public               postgres    false    219   �       �          0    24591    prenotazioni 
   TABLE DATA           z   COPY public.prenotazioni (id_prenotazione, codice_volo_fk, ssn_passeggero_fk, posto, assicurazione, bagaglio) FROM stdin;
    public               postgres    false    221   g       �          0    16400    progetto 
   TABLE DATA           C   COPY public.progetto (pname, pnumber, plocation, dnum) FROM stdin;
    public               postgres    false    217   �       �          0    24580    voli 
   TABLE DATA           �   COPY public.voli (codice_volo, compagnia_aerea, tipo_volo, origine, destinazione, data_volo, orario_previsto, stato, gate_numero) FROM stdin;
    public               postgres    false    218           �           0    0     prenotazioni_id_prenotazione_seq    SEQUENCE SET     N   SELECT pg_catalog.setval('public.prenotazioni_id_prenotazione_seq', 7, true);
          public               postgres    false    220            7           2606    24589    passeggeri passeggeri_pkey 
   CONSTRAINT     Y   ALTER TABLE ONLY public.passeggeri
    ADD CONSTRAINT passeggeri_pkey PRIMARY KEY (ssn);
 D   ALTER TABLE ONLY public.passeggeri DROP CONSTRAINT passeggeri_pkey;
       public                 postgres    false    219            9           2606    24598    prenotazioni prenotazioni_pkey 
   CONSTRAINT     i   ALTER TABLE ONLY public.prenotazioni
    ADD CONSTRAINT prenotazioni_pkey PRIMARY KEY (id_prenotazione);
 H   ALTER TABLE ONLY public.prenotazioni DROP CONSTRAINT prenotazioni_pkey;
       public                 postgres    false    221            1           2606    16404    progetto progetto_pkey 
   CONSTRAINT     Y   ALTER TABLE ONLY public.progetto
    ADD CONSTRAINT progetto_pkey PRIMARY KEY (pnumber);
 @   ALTER TABLE ONLY public.progetto DROP CONSTRAINT progetto_pkey;
       public                 postgres    false    217            3           2606    16406    progetto progetto_pname_key 
   CONSTRAINT     W   ALTER TABLE ONLY public.progetto
    ADD CONSTRAINT progetto_pname_key UNIQUE (pname);
 E   ALTER TABLE ONLY public.progetto DROP CONSTRAINT progetto_pname_key;
       public                 postgres    false    217            5           2606    24584    voli voli_pkey 
   CONSTRAINT     U   ALTER TABLE ONLY public.voli
    ADD CONSTRAINT voli_pkey PRIMARY KEY (codice_volo);
 8   ALTER TABLE ONLY public.voli DROP CONSTRAINT voli_pkey;
       public                 postgres    false    218            :           2606    24599 -   prenotazioni prenotazioni_codice_volo_fk_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.prenotazioni
    ADD CONSTRAINT prenotazioni_codice_volo_fk_fkey FOREIGN KEY (codice_volo_fk) REFERENCES public.voli(codice_volo);
 W   ALTER TABLE ONLY public.prenotazioni DROP CONSTRAINT prenotazioni_codice_volo_fk_fkey;
       public               postgres    false    218    221    4661            ;           2606    24604 0   prenotazioni prenotazioni_ssn_passeggero_fk_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.prenotazioni
    ADD CONSTRAINT prenotazioni_ssn_passeggero_fk_fkey FOREIGN KEY (ssn_passeggero_fk) REFERENCES public.passeggeri(ssn);
 Z   ALTER TABLE ONLY public.prenotazioni DROP CONSTRAINT prenotazioni_ssn_passeggero_fk_fkey;
       public               postgres    false    4663    219    221            �   �   x�]���0Eg�c�<۲��0�I�X�D��Am*��$l�+��Zi=��"�GIh�Y\ ����/�k�4��[|p�)�B%�x��@:²��-�9Z�?�c�_�()e�s��Sק|9�w~���3����g������*)8�����V�S��M;t��@�F�      �   l   x��;
�0E���bd2&~�ID,�"�F����1p��@a����ce396�܋��|�-m{�=���!��jx��{�5�p�-��\h1FӉ�~��YԾ�gED��      �      x������ � �      �   �  x�u��n�0E��W�2��$o&�^�ʨ�2�4:<�5�9�U��5i.���>{� 	�J�
��V�hYl�'(���V8����Ed4�ɘ���Uzμ��C�=P��J�S��f��dLy��ؐS4���^�'�J�� �&�>u��e)���@KniQ���|�;�L��O�>�4��F6?�Ĝ�� �,�[0y>��/
O�7}�����v�%h�&�2/'Xv#�vq<����8�턋��Q�Iz����]$�-��NCd8��t�9�/����4�Pg��i{]�~K�%�bՌ���sS�[�jY�ł�d�b�\�^����NlR��Nn����*�U�]UP߹���^��ò��o�xw�Ϝ�w��9^
�U9NŴ�ZO�+���QM�.^,���]pV��_�8W�iB�%S��nT&?����|�8�XwyΏ�H���a�o
e�T���u����}�eK�     